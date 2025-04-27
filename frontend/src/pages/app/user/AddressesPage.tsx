import { useGetAddressesByUser } from "@/features/app/user/api/getAddressesByUser.ts";
import { AddressesList } from "@/features/app/user/components/AddressesList.tsx";
import { CreateAddressActionIcon } from "@/features/app/user/components/CreateAddressActionIcon.tsx";
import { useAuth } from "@/hooks/useAuth.ts";
import { Flex, Loader, Text, Title } from "@mantine/core";

export function AddressesPage() {
    const { user } = useAuth();

    const getAddressesByUserQuery = useGetAddressesByUser({ userId: user!.id.toString() });

    if (getAddressesByUserQuery.isPending) {
        return (
            <Flex align="center" justify="center" h="100vh">
                <Loader type="bars" size="md"/>
            </Flex>
        );
    }

    if (getAddressesByUserQuery.isError) {
        console.log("Error fetching user addresses", getAddressesByUserQuery.error);
        return (
            <Text c="red.5">
                There was an error when fetching your addresses. Please refresh and try again.
            </Text>
        );
    }

    const addresses = getAddressesByUserQuery.data?.data;

    return (
        <>
            <title>{ `My Addresses | ReSellMart` }</title>

            <Flex align="center" justify="center">
                <Title>
                    My Addresses
                </Title>

                <CreateAddressActionIcon/>
            </Flex>

            <AddressesList addresses={ addresses }/>
        </>
    );
}