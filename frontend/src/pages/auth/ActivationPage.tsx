import { api } from "@/lib/api-client.ts";
import { Button, Container, Flex, Loader, Paper, Text } from "@mantine/core";
import { IconArrowBack, IconCircleCheckFilled, IconExclamationCircleFilled } from "@tabler/icons-react";
import axios from "axios";
import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router";

export function ActivationPage() {
    const [searchParams] = useSearchParams();
    const activationCode = searchParams.get("code");

    const [loading, setLoading] = useState(true);
    const [showSuccess, setShowSuccess] = useState(false);
    const [showError, setShowError] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        async function activateAccount() {
            try {
                await api.post(`api/auth/activation?code=${ activationCode }`);
                setShowSuccess(true);
            } catch (error) {
                console.error("Activation Error: ", error);
                setShowError(true);

                if (axios.isAxiosError(error) && error.response?.status === 404) {
                    setError("Invalid activation code");
                } else if (axios.isAxiosError(error) && error.response?.status === 400 &&
                    error.response?.data.message === "Activation code has expired. A new email has been sent"
                ) {
                    setError("Activation code has expired. A new email has been sent to your address");
                } else {
                    setError("There was an unexpected error when activating your account. Please try again later");
                }
            }
        }

        activateAccount().then(() => setLoading(false));
    }, [activationCode]);

    return (
        <Flex justify="center" align="center" h="100vh">
            <Container size={ 420 }>
                <Paper withBorder shadow="lg" p={ 30 } radius="md">
                    { loading &&
                        <Loader type="dots" size={ 40 }/>
                    }

                    { showSuccess &&
                        <Flex direction="column" gap="xs" mt="sm" align="center" justify="center">
                            <IconCircleCheckFilled color="teal" size={ 50 }/>
                            <Text size="lg" fw="bold">
                                Account successfully activated!
                            </Text>

                            <Button
                                leftSection={ <IconArrowBack/> } fullWidth
                                variant="light" mt="lg"
                                component={ Link } to="/auth/login"
                            >
                                To login
                            </Button>
                        </Flex>
                    }

                    { showError &&
                        <Flex direction="column" gap="xs" mt="sm" align="center" justify="center">
                            <IconExclamationCircleFilled color="red" size={ 50 }/>
                            <Text size="lg" fw="bold">
                                { error }
                            </Text>

                            <Button
                                leftSection={ <IconArrowBack/> } fullWidth
                                variant="light" mt="lg"
                                component={ Link } to="/auth/login"
                            >
                                To login
                            </Button>
                        </Flex>
                    }
                </Paper>
            </Container>
        </Flex>
    );
}