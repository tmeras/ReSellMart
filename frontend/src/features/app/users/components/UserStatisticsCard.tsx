import { useGetUserStatistics } from "@/features/app/users/api/getUserStatistics.ts";
import { Flex, Loader, Paper, Text, Title } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconPackage, IconUser, IconX } from "@tabler/icons-react";

export function UserStatisticsCard() {
    const getUserStatisticsQuery = useGetUserStatistics();

    if (getUserStatisticsQuery.isError) {
        console.log("Error fetching user statistics:", getUserStatisticsQuery.error);

        notifications.show({
            title: "Something went wrong", message: "There was an error fetching user statistics. Please refresh and try again.",
            color: "red", icon: <IconX/>, withBorder: true
        });
    }

    return (
        <Paper radius="md" p="md" withBorder>
            <Flex direction="column" align="center" gap={3}>
                <IconUser size={50}/>

                { getUserStatisticsQuery.isPending ? (
                    <Loader size="sm" />
                ) : getUserStatisticsQuery.isError ? (
                    <Text c="red">Could not fetch</Text>
                ) : (
                    <Text size="xl" >
                        {getUserStatisticsQuery.data?.data.monthlyRegisteredUsers}
                    </Text>
                )}
                <Title order={4}>
                    Users Registered
                </Title>
            </Flex>
        </Paper>
    );
}