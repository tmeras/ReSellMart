import {
    Anchor,
    Button,
    Container,
    Flex,
    Image,
    Paper,
    PasswordInput,
    Select,
    Switch,
    Text,
    TextInput,
    Title
} from "@mantine/core";
import { z } from "zod";
import { Country } from "country-state-city";
import { useMemo, useState } from "react";
import { useForm, zodResolver } from "@mantine/form";
import { PasswordInputWithStrength } from "../../../components/inputs/PasswordInputWithStrength.tsx";
import { IconAlertCircle, IconArrowBack, IconShieldLock, IconX } from "@tabler/icons-react";
import { Link } from "react-router";
import { api } from "../../../lib/api-client.ts";
import { RegistrationResponse } from "../../../types/api.tsx";
import axios from "axios";
import { notifications } from "@mantine/notifications";

export const RegistrationForm = () => {
    const [password, setPassword] = useState("");
    const [formComplete, setFormComplete] = useState(false);
    const [qrImageUri, setQrImageUri] = useState("");

    const countries = useMemo(() => {
        const countries = Country.getAllCountries()
        return countries.map((country) => country.name)
    }, []);

    const registerInputSchema = z.object({
        name: z.string().min(1, "Name is required"),
        email: z.string().email("Invalid email address"),
        password: z.string()
            .min(8, "Password must be at least 8 characters long")
            .regex(
                RegExp("^(?=.*[a-z])(?=.*[A-Z])(?=.*[$&+,:;=?@#|'<>.^*()%!-]).*$"),
                "Password must contain at least one uppercase letter, one lowercase letter, and one special character."
            ),
        homeCountry: z.string().min(1, "Home country is required"),
        mfaEnabled: z.boolean().default(false)
    })

    const form = useForm({
        mode: "uncontrolled",
        initialValues: {
            name: "",
            email: "",
            password: "",
            confirmPassword: "",
            homeCountry: "",
            mfaEnabled: false,
        },
        validate: zodResolver(registerInputSchema),
        onValuesChange: (values) => {
            setPassword(values.password);
            console.log("Form state: ", values);
        },
    })

    const handleSubmit = async (values: typeof form.values) => {
        try {
            if (values.confirmPassword !== values.password) {
                form.setFieldError("password", "");
                form.setFieldError("confirmPassword", "Passwords must match");
                return;
            }

            const response =
                await api.post<RegistrationResponse>("api/auth/registration", values);
            console.log("Registration result", response.data);

            if (response.data.mfaEnabled)
                setQrImageUri(response.data.qrImageUri!);

            setFormComplete(true);
        } catch (error) {
            console.log("Registration error", error);

            if (axios.isAxiosError(error) && error.response?.status === 409) {
                form.setFieldError("email", "An account has already been created using this email");
            } else {
                notifications.show({
                    title: "Something went wrong", message: "Please retry registration",
                    color: "red", icon: <IconX/>, withBorder: true
                });
            }
        }
    }

    if (formComplete)
        return (
            <Flex justify="center" align="center" h="100vh">
                <Container size="400">
                    <Paper withBorder shadow="lg" p={ 30 } radius="md">
                        <Flex direction="column" align="center" justify="center">
                            <Title ta="center" order={ 2 }>
                                Registration Complete!
                            </Title>

                            { qrImageUri &&
                                <>
                                    <Flex gap="xs" mt="sm" align="center" justify="center">
                                        <IconAlertCircle color="red" size={ 25 }/>
                                        <Text size="lg" c="red" fw="bold">
                                            IMPORTANT
                                        </Text>
                                    </Flex>
                                    <Text size="lg" ta="center" mt="xs">
                                        Please scan the following QR code using your preferred authenticator app
                                        to be able to sign in using MFA
                                    </Text>
                                    <Image src={ qrImageUri } w={ 250 } h={ 250 }/>
                                </>
                            }

                            <Text size="lg" ta="center" mt="xs">
                                An email has { qrImageUri && <>also</> } been sent to your address to activate your
                                account
                            </Text>

                            <Button
                                leftSection={ <IconArrowBack/> } fullWidth
                                variant="light" mt="xl"
                                component={ Link } to="/auth/login"
                            >
                                To login
                            </Button>
                        </Flex>
                    </Paper>
                </Container>
            </Flex>
        );

    return (
        <Flex justify="center" align="center" h="100vh">
            <Container size="400">
                <Paper withBorder shadow="lg" p={ 30 } radius="md">
                    <Title ta="center" order={ 2 }>
                        ReSellMart
                    </Title>
                    <Text size="md" c="dimmed" ta="center" mt="xs">
                        Register to begin browsing and selling second-hand goods
                    </Text>

                    <form onSubmit={ form.onSubmit(handleSubmit) }>
                        <TextInput
                            mt="sm"
                            label="Name" required
                            key={ form.key("name") }
                            { ...form.getInputProps("name") }
                        />

                        <TextInput
                            mt="sm"
                            label="Email" required
                            key={ form.key("email") }
                            { ...form.getInputProps("email") }
                        />

                        <PasswordInputWithStrength
                            mt="sm"
                            label="Password" required
                            value={ password }
                            key={ form.key("password") }
                            { ...form.getInputProps("password") }
                        />

                        <PasswordInput
                            mt="sm"
                            label="Confirm password" required
                            key={ form.key("confirmPassword") }
                            { ...form.getInputProps("confirmPassword") }
                        />

                        <Select
                            mt="sm"
                            label="Home country" placeholder="Pick a country"
                            required searchable
                            data={ countries }
                            key={ form.key("homeCountry") }
                            { ...form.getInputProps("homeCountry") }
                        />

                        <Switch
                            mt="md" size="sm"
                            label={
                                <span style={ { display: 'flex', alignItems: 'center' } }>
                                    Enable MFA
                                    <IconShieldLock size={ 22 } style={ { marginLeft: 5 } }/>
                                </span>
                            }
                            key={ form.key("mfaEnabled") }
                            { ...form.getInputProps("mfaEnabled") }
                        />

                        <Button fullWidth mt="xl" type="submit" loading={ form.submitting }>
                            Register
                        </Button>
                    </form>

                    <Text c="dimmed" size="sm" ta="center" mt="sm">
                        Already registered?{ ' ' }
                        <Anchor size="sm" component={ Link } to="/auth/login">
                            Sign in
                        </Anchor>
                    </Text>
                </Paper>
            </Container>
        </Flex>
    );
}