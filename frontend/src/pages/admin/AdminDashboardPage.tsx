import { paths } from "@/config/paths.ts";
import { OrderStatisticsCard } from "@/features/app/orders/components/OrderStatisticsCard.tsx";
import { ProductStatisticsCard } from "@/features/app/products/components/ProductStatisticsCard.tsx";
import { UserStatisticsCard } from "@/features/app/users/components/UserStatisticsCard.tsx";
import { Button, Flex, Paper, ScrollArea, Title } from "@mantine/core";
import { IconCategoryPlus, IconPackages, IconUsers } from "@tabler/icons-react";
import { Link } from "react-router";

export function AdminDashboardPage() {
    return (
        <>
            <title>{ `Admin Dashboard | ReSellMart` }</title>

            <Title ta="center" mb="xl">
                Dashboard
            </Title>

            <Flex justify="center">
                <Paper radius="md" p="md" withBorder w="fit-content">
                    <Title
                        ta={{base: "center", md: "start"}}
                        order={2} mb="md"
                    >
                        Monthly Statistics
                    </Title>

                    <Flex
                        direction={ { base: "column", md: "row" } }
                        gap="md"
                    >
                        <UserStatisticsCard />

                        <ProductStatisticsCard />

                        <OrderStatisticsCard />
                    </Flex>
                </Paper>
            </Flex>

            <Flex
                gap="md" mt="lg"
                align={{base: "center", md: "start"}}
                justify={ { base: "start", md: "center" } }
                direction={ { base: "column", md: "row" } }
            >
                <Button
                    leftSection={<IconCategoryPlus />} size="compact-lg" w={250}
                    component={Link} to={paths.admin.categories.getHref()}
                >
                    Manage Categories
                </Button>

                <Button
                    leftSection={<IconPackages />} size="compact-lg" w={250}
                    component={Link} to={paths.admin.products.getHref()}
                >
                    Manage Products
                </Button>

                <Button
                    leftSection={<IconUsers />} size="compact-lg" w={250}
                    component={Link} to={paths.admin.users.getHref()}
                >
                    Manage Users
                </Button>
            </Flex>
        </>
    );
}