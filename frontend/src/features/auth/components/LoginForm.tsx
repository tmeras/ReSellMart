import { paths } from "@/config/paths.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { api } from "@/lib/apiClient.ts";
import { AuthenticationResponse } from "@/types/api.ts";
import {
    Anchor,
    Button,
    Container,
    Flex,
    Modal,
    Paper,
    PasswordInput,
    PinInput,
    Text,
    TextInput,
    Title
} from "@mantine/core";
import { useForm, zodResolver } from "@mantine/form";
import { notifications } from "@mantine/notifications";
import { IconX } from "@tabler/icons-react";
import axios from "axios";
import { FormEvent, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router";
import { z } from "zod";

export function LoginForm() {
    const [searchParams] = useSearchParams();
    const redirectTo = searchParams.get("redirectTo");

    const { setAccessToken, setIsLoadingUser } = useAuth();
    const navigate = useNavigate();

    const [otp, setOtp] = useState({
        value: "",
        showModal: false,
        error: false
    });

    const loginInputSchema = z.object({
        email: z.string().email("Invalid email address"),
        password: z.string().min(1, "Required")
    });

    const form = useForm({
        mode: "uncontrolled",
        initialValues: {
            email: "",
            password: ""
        },
        validate: zodResolver(loginInputSchema)
    });

    async function handleFormSubmit(values: typeof form.values) {
        try {
            const response =
                await api.post<AuthenticationResponse>("api/auth/login", values);

            if (response.data.isMfaEnabled) {
                setOtp({
                    value: "",
                    showModal: true,
                    error: false
                });
                return;
            }

            console.log("login result", response.data);
            handleLoginSuccess(response.data);
        } catch (error) {
            console.log("Login error", error);

            if (axios.isAxiosError(error) && error.response?.status === 401 &&
                error.response?.data.message === "Bad credentials"
            ) {
                notifications.show({
                    title: "Bad credentials", message: "Invalid email or password",
                    color: "red", icon: <IconX/>, withBorder: true
                });
            } else if (axios.isAxiosError(error) && error.response?.status === 403 &&
                error.response?.data.message === "User is disabled"
            ) {
                notifications.show({
                    title: "Account disabled",
                    message: "Please access the activation email sent to your address to activate your account",
                    color: "red", icon: <IconX/>, withBorder: true
                });
            } else {
                notifications.show({
                    title: "Something went wrong", message: "Please try logging in again",
                    color: "red", icon: <IconX/>, withBorder: true
                });
            }
        }
    }

    async function handleOtpSubmit(e: FormEvent<HTMLFormElement>) {
        e.preventDefault();

        if (otp.value.length !== 6) {
            setOtp({ ...otp, error: true });
            return;
        }

        console.log("Submitting OTP", { ...form.getValues(), otp: otp.value });
        const data = { ...form.getValues(), otp: otp.value };

        try {
            const response = await api.post("api/auth/verification", data);
            handleLoginSuccess(response.data);
        } catch (error) {
            console.log("OTP error", error);

            if (axios.isAxiosError(error) && error.response?.status === 401 &&
                error.response?.data.message === "OTP is not valid"
            ) {
                setOtp({ ...otp, error: true });
                notifications.show({
                    title: "Bad credentials", message: "OTP is invalid",
                    color: "red", icon: <IconX/>, withBorder: true
                });
            } else {
                notifications.show({
                    title: "Something went wrong", message: "Please retry login",
                    color: "red", icon: <IconX/>, withBorder: true
                });
            }
        }
    }

    function handleLoginSuccess(data: AuthenticationResponse) {
        setAccessToken(data.accessToken);
        setIsLoadingUser(true);

        if (redirectTo)
            navigate(redirectTo, { replace: true });
        else
            navigate(paths.app.products.path, { replace: true });
    }

    if (otp.showModal)
        return (
            <Modal
                opened={ otp.showModal } onClose={ () => setOtp({ ...otp, showModal: false }) }
                size="auto" title="Enter OTP" centered
            >
                <form onSubmit={ handleOtpSubmit }>
                    <PinInput
                        size="md" type="number" length={ 6 } data-autofocus
                        error={ otp.error } value={ otp.value }
                        onChange={ (value) => setOtp({ ...otp, value: value }) }
                    />

                    <Button fullWidth mt="xl" type="submit">
                        Submit
                    </Button>
                </form>
            </Modal>
        );

    return (
        <Flex justify="center" align="center" h="80vh">
            <Container size={ 420 }>
                <Paper withBorder shadow="lg" p={ 30 } radius="md">
                    <Title ta="center">
                        ReSellMart
                    </Title>

                    <form onSubmit={ form.onSubmit(handleFormSubmit) }>
                        <TextInput
                            label="Email" required withAsterisk={ false }
                            key={ form.key("email") }
                            { ...form.getInputProps("email") }
                        />

                        <PasswordInput
                            mt="sm"
                            label="Password" required withAsterisk={ false }
                            key={ form.key("password") }
                            { ...form.getInputProps("password") }
                        />

                        <Flex justify="space-between" mt="lg">
                            {/*<Anchor component="button" size="sm">
                                Forgot password?
                            </Anchor>*/ }
                        </Flex>

                        <Button fullWidth mt="lg" type="submit" loading={ form.submitting }>
                            Sign in
                        </Button>
                    </form>

                    <Text c="dimmed" size="sm" ta="center" mt="sm">
                        Don't have an account yet?{ ' ' }
                        <Anchor size="sm" component={ Link } to="/auth/register">
                            Create account
                        </Anchor>
                    </Text>
                </Paper>
            </Container>
        </Flex>
    );
}
