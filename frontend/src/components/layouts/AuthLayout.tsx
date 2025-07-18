import { DarkModeButton } from "@/components/ui/DarkModeButton.tsx";
import { paths } from "@/config/paths.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { AppShell, Button, Flex, Image, Loader, Text } from "@mantine/core";
import { useEffect } from "react";
import { Link, Outlet, useNavigate, useSearchParams } from "react-router";
import imgUrl from "../../assets/logo.png";

export function AuthLayout() {
    const [searchParams] = useSearchParams();
    const redirectTo = searchParams.get('redirectTo');

    const { user, isLoadingUser } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        if (user) {
            if (redirectTo)
                navigate(redirectTo, { replace: true });
            else
                navigate(paths.app.products.path, { replace: true });
        }
    }, [user, navigate, redirectTo]);

    return (
        <AppShell
            header={ { height: 60 } }
            padding="md"
        >
            <AppShell.Header p="md">
                <Flex align="center" justify="space-between">
                    <Flex>
                        <Image radius="md" src={ imgUrl } h={ 30 } w={ 30 } me={ 5 }/>
                        <Text
                            size="lg" variant="gradient" fw={ 700 }
                            gradient={ { from: "paleIndigo.8", to: "paleIndigo.4", deg: 150 } }
                            component={Link} to={paths.home.path}
                        >
                            ReSellMart
                        </Text>
                    </Flex>

                    <Flex align="center">
                        <Button size="xs" component={ Link } to={ paths.auth.login.path }>
                            Sign in
                        </Button>

                        <DarkModeButton/>
                    </Flex>
                </Flex>
            </AppShell.Header>

            <AppShell.Main>
                { isLoadingUser || user ? (
                    <Flex align="center" justify="center" h="100vh" w="100%">
                        <Loader type="bars" size="md"/>
                    </Flex>
                ) : (
                    <Outlet/>
                ) }
            </AppShell.Main>

            <AppShell.Footer p="md" style={ { position: "relative" } }>
                <Flex direction="column" justify="center" align="center">
                    <Flex justify="center" align="center" mb="sm">
                        <Image radius="md" src={ imgUrl } h={ 30 } w={ 30 } me={ 5 }/>
                        <Text
                            size="lg" variant="gradient" fw={ 700 }
                            gradient={ { from: "paleIndigo.8", to: "paleIndigo.4", deg: 150 } }
                        >
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