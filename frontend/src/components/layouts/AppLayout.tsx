import imgUrl from "@/assets/logo.png";
import { DarkModeButton } from "@/components/ui/DarkModeButton.tsx";
import { LogoText } from "@/components/ui/LogoText.tsx";
import { UserMenu } from "@/components/ui/UserMenu.tsx";
import { AppShell, Burger, Flex, Image, Text, Title } from "@mantine/core";
import { useState } from "react";
import { Outlet } from "react-router";

export function AppLayout() {
    const [navBarOpened, setNavBarOpened] = useState(false);

    // TODO: Add error boundary
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
                <Flex direction="column" justify="space-between" h="100%">
                    <div>
                        <Title order={ 3 } c="paleIndigo.5" mb="sm">
                            Buying
                        </Title>
                        ...

                    </div>

                    <UserMenu/>
                </Flex>

            </AppShell.Navbar>

            <AppShell.Main>
                <Outlet/>
            </AppShell.Main>

            <AppShell.Footer p="md" style={ { position: "relative" } }>
                <Flex direction="column" justify="center" align="center">
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