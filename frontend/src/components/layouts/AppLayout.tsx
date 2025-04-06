import imgUrl from "@/assets/logo.png";
import { LogoText } from "@/components/ui/LogoText.tsx";
import { UserMenu } from "@/components/ui/UserMenu.tsx";
import { ActionIcon, AppShell, Burger, Flex, Image, Text, Title, Tooltip, useMantineColorScheme } from "@mantine/core";
import { IconBrightnessDown, IconMoon } from "@tabler/icons-react";
import { useState } from "react";
import { Outlet } from "react-router";

export function AppLayout() {
    const [navBarOpened, setNavBarOpened] = useState(false);
    const { colorScheme, toggleColorScheme } = useMantineColorScheme();

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
                    { colorScheme === "dark" ? (
                        <Tooltip label="Light Mode">
                            <ActionIcon
                                aria-label="Light Mode" onClick={ () => toggleColorScheme() }
                                variant="filled" ms="sm" size="md" mt={ 2 }
                            >
                                <IconBrightnessDown size={ 60 }/>
                            </ActionIcon>
                        </Tooltip>
                    ) : (
                        <Tooltip label="Dark Mode">
                            <ActionIcon
                                aria-label="Dark Mode" onClick={ () => toggleColorScheme() }
                                variant="outline" ms="sm" size="md" mt={ 2 }
                            >
                                <IconMoon size={ 50 }/>
                            </ActionIcon>
                        </Tooltip>
                    ) }
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