import { useGetProductStatistics } from "@/features/app/products/api/getProductStatistics.ts";
import { Flex, Loader, Paper, Text, Title } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconPackage, IconX } from "@tabler/icons-react";

export function ProductStatisticsCard() {
    const getProductStatisticsQuery = useGetProductStatistics();

    if (getProductStatisticsQuery.isError) {
        console.log("Error fetching product statistics:", getProductStatisticsQuery.error);

        notifications.show({
            title: "Something went wrong", message: "There was an error fetching product statistics. Please refresh and try again.",
            color: "red", icon: <IconX/>, withBorder: true
        });
    }

    return (
      <Paper radius="md" p="md" withBorder>
          <Flex direction="column" align="center" gap={3}>
              <IconPackage size={50}/>

              { getProductStatisticsQuery.isPending ? (
                  <Loader size="sm" />
              ) : getProductStatisticsQuery.isError ? (
                  <Text c="red">Could not fetch</Text>
              ) : (
                  <Text size="xl">
                    {getProductStatisticsQuery.data?.data.monthlyListedProducts}
                  </Text>
              )}
              <Title order={4}>
                  Products Listed
              </Title>
          </Flex>
      </Paper>
    );
}