import { DeleteAddressButton } from "@/features/app/user/components/DeleteAddressButton.tsx";
import { UpdateAddressButton } from "@/features/app/user/components/UpdateAddressButton.tsx";
import { AddressResponse } from "@/types/api.ts";
import { ADDRESS_TYPE } from "@/utils/constants.ts";
import { Badge, Card, Flex, Grid, Text } from "@mantine/core";
import { IconBriefcase, IconCreditCard, IconHome, IconTruckDelivery } from "@tabler/icons-react";
import { MainAddressButton } from "./MainAddressButton";


const ADDRESS_TYPE_COLORS = {
    HOME: "teal",
    WORK: "blue",
    BILLING: "orange",
    SHIPPING: "green"
} as const;

const ADDRESS_TYPE_ICONS = {
    HOME: <IconHome size={ 14 }/>,
    WORK: <IconBriefcase size={ 14 }/>,
    BILLING: <IconCreditCard size={ 14 }/>,
    SHIPPING: <IconTruckDelivery size={ 14 }/>
};

export type AddressesListProps = {
    addresses: AddressResponse[];
}

export function AddressesList({ addresses }: AddressesListProps) {

    const addressCards = addresses.map((address) =>
        <Grid.Col span={ { base: 12, sm: 6, md: 4 } } key={ address.id }>
            <Flex justify="center">
                <Card withBorder radius="md" w={ 350 }>
                    <Card.Section inheritPadding withBorder>
                        <Flex align="center" mt="xs" mb="xs">
                            { address.main &&
                                <Badge variant="light" color="indigo" me="sm">
                                    Main Address
                                </Badge>
                            }
                            <Badge
                                variant="light" color={ ADDRESS_TYPE_COLORS[address.addressType] }
                                leftSection={ ADDRESS_TYPE_ICONS[address.addressType] }
                            >
                                { ADDRESS_TYPE[address.addressType] }
                            </Badge>
                        </Flex>
                    </Card.Section>

                    <Card.Section inheritPadding withBorder mih={ 140 }>
                        <Text mt="xs" fw={ 600 }>
                            { address.name }
                        </Text>

                        <Text>{ address.street }</Text>

                        <Text>{ address.city }, { address.state } { address.postalCode }</Text>

                        <Text>{ address.country }</Text>

                        <Text mb="xs">{ address.phoneNumber }</Text>
                    </Card.Section>

                    <Card.Section inheritPadding withBorder>
                        <Flex gap="md" my="xs">
                            { !address.main &&
                                <MainAddressButton addressId={ address.id.toString() }/>
                            }

                            <UpdateAddressButton address={ address }/>

                            <DeleteAddressButton addressId={ address.id.toString() }/>
                        </Flex>
                    </Card.Section>
                </Card>
            </Flex>
        </Grid.Col>
    );

    return (
        <Grid gutter="xl" mt="lg">
            { addressCards }
        </Grid>
    );
}