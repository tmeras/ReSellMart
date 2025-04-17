import imgUrl from "@/assets/logo.png";
import { AppErrorBoundary } from "@/components/error/AppErrorBoundary.tsx";
import { CategoryNavLinks } from "@/components/ui/CategoryNavLinks.tsx";
import { DarkModeButton } from "@/components/ui/DarkModeButton.tsx";
import { UserMenu } from "@/components/ui/UserMenu.tsx";
import { paths } from "@/config/paths.ts";
import { AppShell, Burger, Button, Divider, Flex, Image, NavLink, ScrollArea, Text } from "@mantine/core";
import { IconCirclePlus, IconGridDots } from "@tabler/icons-react";
import { useState } from "react";
import { ErrorBoundary } from "react-error-boundary";
import { Link, NavLink as RouterNavLink, Outlet, useNavigate } from "react-router";

export function AppLayout() {
    const navigate = useNavigate();
    const [navBarOpened, setNavBarOpened] = useState(false);

    return (
        <AppShell
            header={ { height: 60 } }
            navbar={ {
                width: 270,
                breakpoint: "md",
                collapsed: { mobile: !navBarOpened }
            } }
            padding="md"
        >
            <AppShell.Header p="md">
                <Flex align="center" justify="space-between">
                    <Flex>
                        <Burger
                            hiddenFrom="md" size="sm" me="md"
                            opened={ navBarOpened } onClick={ () => setNavBarOpened(!navBarOpened) }
                        />

                        <Image radius="md" src={ imgUrl } h={ 30 } w={ 30 } me="sm"/>
                        <Text
                            size="lg" variant="gradient" fw={ 700 }
                            gradient={ { from: "paleIndigo.8", to: "paleIndigo.4", deg: 150 } }
                            style={ { cursor: "pointer" } }
                            onClick={ () => navigate(paths.app.products.path) }
                        >
                            ReSellMart
                        </Text>
                    </Flex>

                    <DarkModeButton/>
                </Flex>
            </AppShell.Header>

            <AppShell.Navbar p="md">
                <AppShell.Section
                    my="sm"
                    component={ ScrollArea } grow
                    type="hover" scrollbarSize={ 2 }
                >
                    <Text size="xl" fw={ 700 } c="paleIndigo.5" mb="sm">
                        Buy
                    </Text>

                    <NavLink
                        label="All Products"
                        leftSection={ <IconGridDots size={ 18 }/> }
                        component={ RouterNavLink } onClick={ () => setNavBarOpened(false) }
                        to={ paths.app.products.path } end
                    />

                    <CategoryNavLinks closeNavBar={ () => setNavBarOpened(false) }/>

                    <Text size="xl" fw={ 700 } c="paleIndigo.5" mb="sm">
                        Sell
                    </Text>

                    <Button
                        size="compact-md" variant="light"
                        leftSection={ <IconCirclePlus size={ 18 }/> }
                        onClick={ () => setNavBarOpened(false) }
                        component={ Link } to={ paths.app.createProduct.path }
                    >
                        Create product listing
                    </Button>

                </AppShell.Section>

                <AppShell.Section>
                    <UserMenu closeNavBar={ () => setNavBarOpened(false) }/>
                </AppShell.Section>
            </AppShell.Navbar>

            <AppShell.Main>
                <ErrorBoundary FallbackComponent={ AppErrorBoundary }>
                    <Flex direction="column" mih="100vh" w="100%">
                        <Outlet/>

                        <Flex direction="column" mt="auto" w="100%">
                            <Divider size="xs" mb="lg" mt="xl" w="100%"/>

                            <Flex direction="column" justify="center" align="center" w="100%">
                                <Flex mb="sm">
                                    <Image radius="md" src={ imgUrl } h={ 30 } w={ 30 } me="sm"/>
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
                        </Flex>
                    </Flex>
                </ErrorBoundary>
            </AppShell.Main>
        </AppShell>
    );
}