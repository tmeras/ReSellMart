import { CreateAddressActionIcon } from "@/components/ui/CreateAddressActionIcon.tsx";
import { AddressResponse } from "@/types/api.ts";
import { Flex, Paper, Radio, ScrollArea, Text, Title } from "@mantine/core";
import { useEffect } from "react";

export type AddressSelectCardProps = {
    title: string;
    selectedAddressId: string;
    setSelectedAddressId: (selectedAddressId: string) => void;
    addresses: AddressResponse[];
};

export function AddressSelectCard(
    { title, selectedAddressId, setSelectedAddressId, addresses }: AddressSelectCardProps
) {

    // Set main address as default address, if it exists,
    // otherwise set the first address as default
    useEffect(() => {
        const mainAddress =
            addresses.find(address => address.isMain);

        if (mainAddress) {
            setSelectedAddressId(mainAddress.id.toString());
        } else if (addresses.length > 0) {
            setSelectedAddressId(addresses[0].id.toString());
        }
    }, [addresses, setSelectedAddressId]);

    const addressOptions = addresses.map((address) => (
        <Radio.Card radius="md" value={ address.id.toString() } key={ address.id }>
            <Flex align="center" gap="sm" ms="sm">
                <Radio.Indicator/>

                <div>
                    <Text mt="xs" fw={ 600 }>
                        { address.name }
                    </Text>

                    <Text>{ address.street }</Text>

                    <Text>{ address.city }, { address.state } { address.postalCode }</Text>

                    <Text>{ address.country }</Text>

                    <Text mb="xs">{ address.phoneNumber }</Text>
                </div>
            </Flex>
        </Radio.Card>
    ));

    return (
        <Paper
            p="md" radius="sm"
            withBorder shadow="lg"
            w={ { base: 480, xs: 550, md: 650 } }
        >
            <Flex
                direction="column" w="100%"
            >
                <Flex>
                    <Title order={ 2 } mb="sm">
                        { title }
                    </Title>

                    <CreateAddressActionIcon/>
                </Flex>

                { addresses.length === 0 ? (
                    <Text>
                        You do not have any saved addresses. Please add an address to proceed.
                    </Text>
                ) : (
                    <ScrollArea.Autosize mah={ 350 }>
                        <Radio.Group
                            value={ selectedAddressId }
                            onChange={ setSelectedAddressId }
                            w="100%"
                        >
                            <Flex direction="column" gap="sm" w="75%">
                                { addressOptions }
                            </Flex>
                        </Radio.Group>
                    </ScrollArea.Autosize>
                ) }
            </Flex>
        </Paper>
    );
}