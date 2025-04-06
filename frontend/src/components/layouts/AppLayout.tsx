import { ActionIcon, AppShell, Burger, Flex, Image, Text, Tooltip, useMantineColorScheme } from "@mantine/core";
import { IconBrightnessDown, IconMoon } from "@tabler/icons-react";
import { useState } from "react";
import { Outlet } from "react-router";
import imgUrl from "../../assets/logo.png";

export function AppLayout() {
    const [navBarOpened, setNavBarOpened] = useState(false);
    const { colorScheme, toggleColorScheme } = useMantineColorScheme();

    return (
        <AppShell
            header={ { height: 60 } }
            navbar={ {
                width: 250,
                breakpoint: "sm",
                collapsed: { mobile: !navBarOpened }
            } }
            padding="md"
        >
            <AppShell.Header p="md">
                <Flex align="center" justify="space-between">
                    <Flex>
                        <Burger
                            hiddenFrom="sm" size="sm" me="md"
                            opened={ navBarOpened } onClick={ () => setNavBarOpened(!navBarOpened) }
                        />
                        <Image radius="md" src={ imgUrl } h={ 30 } w={ 30 } me="sm"/>
                        <Text size="lg">
                            ReSellMart
                        </Text>
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

            {/*TODO: Logout*/ }
            <AppShell.Navbar p="md">
                Placeholder
            </AppShell.Navbar>

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