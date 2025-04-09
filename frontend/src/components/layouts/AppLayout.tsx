import imgUrl from "@/assets/logo.png";
import { AppErrorBoundary } from "@/components/error/AppErrorBoundary.tsx";
import { CategoryNavLinks } from "@/components/ui/CategoryNavLinks.tsx";
import { DarkModeButton } from "@/components/ui/DarkModeButton.tsx";
import { LogoText } from "@/components/ui/LogoText.tsx";
import { UserMenu } from "@/components/ui/UserMenu.tsx";
import { paths } from "@/config/paths.ts";
import { AppShell, Burger, Flex, Image, NavLink, ScrollArea, Text } from "@mantine/core";
import { IconGridDots } from "@tabler/icons-react";
import { useState } from "react";
import { ErrorBoundary } from "react-error-boundary";
import { NavLink as RouterNavLink, Outlet } from "react-router";

export function AppLayout() {
    const [navBarOpened, setNavBarOpened] = useState(false);

    return (
        <AppShell
            header={ { height: 60 } }
            navbar={ {
                width: 250,
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
                        <LogoText/>
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
                        Buying
                    </Text>

                    <NavLink
                        label="All Products"
                        leftSection={ <IconGridDots size={ 18 }/> }
                        component={ RouterNavLink } onClick={ () => setNavBarOpened(false) }
                        to={ paths.app.products.path } end
                    />

                    <CategoryNavLinks closeNavBar={ () => setNavBarOpened(false) }/>
                </AppShell.Section>

                <AppShell.Section>
                    <UserMenu closeNavBar={ () => setNavBarOpened(false) }/>
                </AppShell.Section>
            </AppShell.Navbar>

            <AppShell.Main>
                <ErrorBoundary FallbackComponent={ AppErrorBoundary }>
                    <Outlet/>
                </ErrorBoundary>
            </AppShell.Main>

            <AppShell.Footer p="md" pos="relative">
                <Flex
                    direction="column" justify="center" align="center"
                    ml={ { base: 0, md: "15%" } }
                >
                    <Flex justify="center" align="center" mb="sm">
                        <Image radius="md" src={ imgUrl } h={ 30 } w={ 30 } me="sm"/>
                        <LogoText/>
                    </Flex>
                    <Text c="dimmed" size="sm">
                        © 2025 ReSellMart. All rights reserved.
                    </Text>
                </Flex>
            </AppShell.Footer>
        </AppShell>
    );
}