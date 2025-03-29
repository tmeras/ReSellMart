import { Link, Outlet, useNavigate, useSearchParams } from "react-router";
import { useEffect } from "react";
import { useAuth } from "../../hooks/useAuth.ts";
import { ActionIcon, AppShell, Button, Flex, Image, Text, Tooltip, useMantineColorScheme } from "@mantine/core";
import imgUrl from "../../assets/logo.png";
import { paths } from "../../config/paths.ts";
import { IconBrightnessDown, IconMoon } from "@tabler/icons-react";

export function AuthLayout() {
    const { user } = useAuth();
    const { colorScheme, toggleColorScheme } = useMantineColorScheme();
    const [searchParams] = useSearchParams();
    const redirectTo = searchParams.get('redirectTo');
    const navigate = useNavigate();

    useEffect(() => {
        if (user)
            if (redirectTo)
                navigate(redirectTo, { replace: true });
            else
                navigate(paths.app.products, { replace: true });
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
                    <Flex>
                        <Button size="xs" component={ Link } to={ paths.auth.login }>
                            Sign in
                        </Button>
                        { colorScheme === "dark" ? (
                            <Tooltip label="Light Mode">
                                <ActionIcon
                                    aria-label="Light Mode" onClick={ () => toggleColorScheme() }
                                    variant="filled" ms="sm" size="md"
                                >
                                    <IconBrightnessDown size={ 60 }/>
                                </ActionIcon>
                            </Tooltip>
                        ) : (
                            <Tooltip label="Dark Mode">
                                <ActionIcon
                                    aria-label="Dark Mode" onClick={ () => toggleColorScheme() }
                                    variant="outline" ms="sm" size="md"
                                >
                                    <IconMoon size={ 50 }/>
                                </ActionIcon>
                            </Tooltip>
                        ) }
                    </Flex>
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