import { useGetOrderStatistics } from "@/features/app/orders/api/getOrderStatistics.ts";
import { Flex, Loader, Paper, Text, Title } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import {
    IconCoinPound,
    IconPackage,
    IconPackages,
    IconReceipt,
    IconReceipt2,
    IconTruckDelivery,
    IconX
} from "@tabler/icons-react";

export function OrderStatisticsCard() {
    const getOrderStatisticsQuery = useGetOrderStatistics();

    if (getOrderStatisticsQuery.isError) {
        console.error("Error fetching order statistics:", getOrderStatisticsQuery.error);

        notifications.show({
            title: "Something went wrong", message: "There was an error fetching order statistics. Please refresh and try again.",
            color: "red", icon: <IconX/>, withBorder: true
        });
    }

    return (
        <Paper radius="md" p="md" withBorder>
            <Flex gap="lg">
                <Flex direction="column" align="center" gap={3}>
                    <IconReceipt2 size={50}/>

                    { getOrderStatisticsQuery.isPending ? (
                        <Loader size="sm" />
                    ) : getOrderStatisticsQuery.isError ? (
                        <Text c="red">Could not fetch</Text>
                    ) : (
                        <Text size="xl" >
                            {getOrderStatisticsQuery.data?.data.monthlyOrderCount}
                        </Text>
                    )}
                    <Title order={4}>
                        Orders Placed
                    </Title>
                </Flex>

                <Flex direction="column" align="center" gap={3}>
                    <IconTruckDelivery size={50}/>

                    { getOrderStatisticsQuery.isPending ? (
                        <Loader size="sm" />
                    ) : getOrderStatisticsQuery.isError ? (
                        <Text c="red">Could not fetch</Text>
                    ) : (
                        <Text size="xl" >
                            {getOrderStatisticsQuery.data?.data.monthlyProductSales}
                        </Text>
                    )}
                    <Title order={4}>
                        Products Ordered
                    </Title>
                </Flex>

                <Flex direction="column" align="center" gap={3}>
                    <IconCoinPound size={50}/>

                    { getOrderStatisticsQuery.isPending ? (
                        <Loader size="sm" />
                    ) : getOrderStatisticsQuery.isError ? (
                        <Text c="red">Could not fetch</Text>
                    ) : (
                        <Text size="xl" >
                            £ {getOrderStatisticsQuery.data?.data.monthlyRevenue}
                        </Text>
                    )}
                    <Title order={4}>
                        Revenue
                    </Title>
                </Flex>
            </Flex>
        </Paper>
    );
}