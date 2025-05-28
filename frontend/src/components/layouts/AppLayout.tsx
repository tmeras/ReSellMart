import imgUrl from "@/assets/logo.png";
import { AppErrorBoundary } from "@/components/error/AppErrorBoundary.tsx";
import { CategoryNavLinks } from "@/components/ui/CategoryNavLinks.tsx";
import { DarkModeButton } from "@/components/ui/DarkModeButton.tsx";
import { UserMenu } from "@/components/ui/UserMenu.tsx";
import { paths } from "@/config/paths.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { AppShell, Burger, Button, Divider, Flex, Image, NavLink, ScrollArea, Text } from "@mantine/core";
import {
    IconCash,
    IconCategoryPlus,
    IconCirclePlus,
    IconGridDots,
    IconPackage,
    IconReceipt,
    IconShoppingCart,
    IconUsers
} from "@tabler/icons-react";
import { useState } from "react";
import { ErrorBoundary } from "react-error-boundary";
import { Link, NavLink as RouterNavLink, Outlet, useNavigate } from "react-router";

export function AppLayout() {
    const navigate = useNavigate();
    const { user } = useAuth();
    const [navBarOpened, setNavBarOpened] = useState(false);

    const isAdmin = user!.roles.some(role => role.name.toLowerCase() === "admin");

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

                        <Image radius="md" src={ imgUrl } h={ 30 } w={ 30 } me={ 5 }/>
                        <Text
                            size="lg" variant="gradient" fw={ 700 }
                            gradient={ { from: "paleIndigo.8", to: "paleIndigo.4", deg: 150 } }
                            style={ { cursor: "pointer" } }
                            onClick={ () => navigate(paths.app.products.path) }
                        >
                            ReSellMart
                        </Text>
                    </Flex>

                    <Button
                        variant="outline" size="compact-md" me={ 5 }
                        leftSection={ <IconShoppingCart size={ 18 }/> }
                        component={ Link } to={ paths.app.cart.path }
                    >
                        Cart
                    </Button>
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

                    <NavLink
                        label="My Purchases"
                        leftSection={ <IconReceipt size={ 18 }/> }
                        component={ RouterNavLink } onClick={ () => setNavBarOpened(false) }
                        to={ paths.app.purchases.path } end
                    />

                    <Text size="xl" fw={ 700 } c="paleIndigo.5">
                        Sell
                    </Text>

                    <NavLink
                        label="My Products"
                        leftSection={ <IconPackage size={ 18 }/> }
                        component={ RouterNavLink } onClick={ () => setNavBarOpened(false) }
                        to={ paths.app.sellerProducts.path } end
                    />

                    <NavLink
                        label="My Sales"
                        leftSection={ <IconCash size={ 18 }/> }
                        component={ RouterNavLink } onClick={ () => setNavBarOpened(false) }
                        to={ paths.app.sales.path } end
                    />

                    <NavLink
                        label="New Product Listing"
                        leftSection={ <IconCirclePlus size={ 18 }/> }
                        component={ RouterNavLink } onClick={ () => setNavBarOpened(false) }
                        to={ paths.app.createProduct.path }
                    />


                    { isAdmin &&
                        <>
                            <Text size="xl" fw={ 700 } c="paleIndigo.5">
                                Admin
                            </Text>

                            <NavLink
                                label="Product Categories Management"
                                leftSection={ <IconCategoryPlus size={ 18 }/> }
                                component={ RouterNavLink } onClick={ () => setNavBarOpened(false) }
                                to={ paths.admin.categories.path }
                            />

                            <NavLink
                                label="Users Management"
                                leftSection={ <IconUsers size={ 18 }/> }
                                component={ RouterNavLink } onClick={ () => setNavBarOpened(false) }
                                to={ paths.admin.users.path }
                            />
                        </>
                    }
                </AppShell.Section>

                <AppShell.Section>
                    <Flex justify="space-between" align="center">
                        <UserMenu closeNavBar={ () => setNavBarOpened(false) }/>

                        <DarkModeButton/>
                    </Flex>
                </AppShell.Section>
            </AppShell.Navbar>

            <AppShell.Main>
                <ErrorBoundary FallbackComponent={ AppErrorBoundary }>
                    <Flex direction="column">
                        <Flex direction="column" mih="100vh">
                            <Outlet/>
                        </Flex>

                        <Flex direction="column" mt={ 50 }>
                            <Divider size="xs" mb="lg" mt="xl"/>

                            <Flex direction="column" justify="center" align="center" w="100%">
                                <Flex mb="sm">
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
                        </Flex>
                    </Flex>
                </ErrorBoundary>
            </AppShell.Main>
        </AppShell>
    );
}