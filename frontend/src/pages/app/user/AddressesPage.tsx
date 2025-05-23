import { useGetAddressesByUser } from "@/api/addresses/getAddressesByUser.ts";
import { CreateAddressActionIcon } from "@/components/ui/CreateAddressActionIcon.tsx";
import { AddressesList } from "@/features/app/addresses/components/AddressesList.tsx";
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

                <CreateAddressActionIcon size="lg"/>
            </Flex>

            <AddressesList addresses={ addresses }/>
        </>
    );
}