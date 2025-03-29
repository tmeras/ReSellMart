import { useState } from "react";
import { AppShell, Burger, Flex, Image, Text } from "@mantine/core";
import imgUrl from "../../assets/logo.png";
import { Outlet } from "react-router";

export function AppLayout() {
    const [navBarOpened, setNavBarOpened] = useState(false);

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
                <Flex align="center">
                    <Burger
                        hiddenFrom="sm" size="sm" me="md"
                        opened={ navBarOpened } onClick={ () => setNavBarOpened(!navBarOpened) }
                    />
                    <Image radius="md" src={ imgUrl } h={ 30 } w={ 30 } me="sm"/>
                    <Text size="lg">
                        ReSellMart
                    </Text>
                </Flex>
            </AppShell.Header>

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