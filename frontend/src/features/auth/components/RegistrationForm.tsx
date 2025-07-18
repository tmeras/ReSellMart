import { PasswordInputWithStrength } from "@/components/form/PasswordInputWithStrength.tsx";
import { paths } from "@/config/paths.ts";
import { api } from "@/lib/apiClient.ts";
import { RegistrationResponse } from "@/types/api.ts";
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
import { useForm, zodResolver } from "@mantine/form";
import { notifications } from "@mantine/notifications";
import { IconAlertCircle, IconArrowBack, IconShieldLock, IconX } from "@tabler/icons-react";
import axios from "axios";
import { Country } from "country-state-city";
import { useMemo, useState } from "react";
import { Link } from "react-router";
import { z } from "zod";

export const RegistrationForm = () => {
    const [password, setPassword] = useState("");
    const [formComplete, setFormComplete] = useState(false);
    const [qrImageUri, setQrImageUri] = useState("");

    const countryOptions = useMemo(() => {
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
        isMfaEnabled: z.boolean().default(false)
    });

    const form = useForm({
        mode: "uncontrolled",
        initialValues: {
            name: "",
            email: "",
            password: "",
            confirmPassword: "",
            homeCountry: "",
            isMfaEnabled: false
        },
        validate: zodResolver(registerInputSchema),
        onValuesChange: (values) => {
            setPassword(values.password);
        },
    });

    async function handleSubmit(values: typeof form.values) {
        try {
            if (values.confirmPassword !== values.password) {
                form.setFieldError("password", "");
                form.setFieldError("confirmPassword", "Passwords must match");
                return;
            }

            const response =
                await api.post<RegistrationResponse>("api/auth/registration", values);
            console.log("Registration result", response.data);

            if (response.data.isMfaEnabled)
                setQrImageUri(response.data.qrImageUri!);

            setFormComplete(true);
        } catch (error) {
            console.log("Registration error", error);

            if (axios.isAxiosError(error) && error.response?.status === 409) {
                form.setFieldError("email", "An account has already been created using this email");
            } else {
                notifications.show({
                    title: "Something went wrong", message: "Please try registering again",
                    color: "red", icon: <IconX/>, withBorder: true
                });
            }
        }
    }

    if (formComplete)
        return (
            <Flex justify="center" align="center" h="90vh">
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
                                        <Text size="lg" c="red.8" fw="bold">
                                            IMPORTANT
                                        </Text>
                                    </Flex>

                                    <Text size="lg" ta="center" my="xs">
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
                                component={ Link } to={ paths.auth.login.path }
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
                    <Text
                        ta="center" variant="gradient" fw={ 700 } component={ Title }
                        gradient={ { from: "paleIndigo.8", to: "paleIndigo.4", deg: 150 } }
                    >
                        ReSellMart
                    </Text>
                    <Text size="md" c="dimmed" ta="center" mt="xs">
                        Register to begin browsing and selling second-hand goods
                    </Text>

                    <form onSubmit={ form.onSubmit(handleSubmit) }>
                        <TextInput
                            mt="sm"
                            label="Name" required withAsterisk={ false }
                            key={ form.key("name") }
                            { ...form.getInputProps("name") }
                        />

                        <TextInput
                            mt="sm"
                            label="Email" required withAsterisk={ false }
                            key={ form.key("email") }
                            { ...form.getInputProps("email") }
                        />

                        <PasswordInputWithStrength
                            mt="sm"
                            label="Password" required withAsterisk={ false }
                            value={ password }
                            key={ form.key("password") }
                            { ...form.getInputProps("password") }
                        />

                        <PasswordInput
                            mt="sm"
                            label="Confirm password" required withAsterisk={ false }
                            key={ form.key("confirmPassword") }
                            { ...form.getInputProps("confirmPassword") }
                        />

                        <Select
                            mt="sm"
                            label="Home country" placeholder="Pick a country"
                            required withAsterisk={ false } searchable
                            data={ countryOptions }
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
                            key={ form.key("isMfaEnabled") }
                            { ...form.getInputProps("isMfaEnabled") }
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