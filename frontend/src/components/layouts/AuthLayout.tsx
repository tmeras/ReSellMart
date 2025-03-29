import { Link, Outlet, useNavigate, useSearchParams } from "react-router";
import { useEffect } from "react";
import { useAuth } from "../../hooks/useAuth.ts";
import { AppShell, Button, Flex, Image, Text } from "@mantine/core";
import imgUrl from "../../assets/logo.png";
import { paths } from "../../config/paths.ts";

export function AuthLayout() {
    const { user } = useAuth();

    const [searchParams] = useSearchParams();
    const redirectTo = searchParams.get('redirectTo');
    const navigate = useNavigate();

    useEffect(() => {
        if (user)
            if (redirectTo)
                navigate(redirectTo, { replace: true });
            else
                navigate("/app/products", { replace: true });
    }, [user, navigate, redirectTo]);

    return (
        <AppShell
            header={ { height: 60 } }
            padding="md"
        >
            <AppShell.Header p="md">
                <Flex align="center" justify="space-between">
                    <Flex>
                        <Image radius="md" src={ imgUrl } h={ 30 } w={ 30 } me="sm"/>
                        <Text size="lg">
                            ReSellMart
                        </Text>
                    </Flex>
                    <Button size="xs" component={ Link } to={ paths.auth.login }>
                        Sign in
                    </Button>
                </Flex>
            </AppShell.Header>

            <AppShell.Main>
                <Outlet/>
            </AppShell.Main>

            <AppShell.Footer p="md" style={ { position: "relative" } }>
                <Flex direction="column" justify="center" align="center">
                    <Flex justify="center" align="center" mb="sm">
                        <Image radius="md" src={ imgUrl } h={ 30 } w={ 30 } me="sm"/>
                        <Text size="lg">
                            ReSellMart
                        </Text>
                    </Flex>
                    <Text c="dimmed" size="sm">
                        © 2025 ReSellMart. All rights reserved.
                    </Text>
                </Flex>
            </AppShell.Footer>
        </AppShell>
    );
}