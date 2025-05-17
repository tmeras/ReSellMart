import { OrderItemStatusStepper } from "@/features/app/orders/components/OrderItemStatusStepper.tsx";
import { OrderResponse } from "@/types/api.ts";
import { base64ToDataUri } from "@/utils/fileUtils.ts";
import { Button, Card, Divider, Flex, Image, Popover, Text, useMantineColorScheme } from "@mantine/core";
import { IconArrowDown } from "@tabler/icons-react";
import { useState } from "react";

export type SaleCardProps = {
    sale: OrderResponse;
}

export function SaleCard({ sale }: SaleCardProps) {
    const { colorScheme } = useMantineColorScheme();
    const [popoverOpened, setPopoverOpened] = useState(false);

    const placedAt = new Date(sale.placedAt);

    return (
        <Card
            withBorder radius="md" p="md"
            w={ { base: 450, xs: 650 } }
        >
            <Card.Section
                inheritPadding withBorder
                bg={ colorScheme === "dark" ? "dark.4" : "gray.2" }
            >
                <Flex my={ 5 }>
                    <Text size="xs" c="dimmed" ms="auto">
                        Placed on: { placedAt.toLocaleDateString() }
                    </Text>
                </Flex>

                <Flex align="center" justify="space-between">
                    <Text size="lg" fw={ 700 }>
                        Order
                    </Text>

                    <Text size="lg" fw={ 700 }>
                        Deliver To
                    </Text>

                    <Text size="lg" fw={ 700 }>
                        Total
                    </Text>
                </Flex>

                <Flex align="center" justify="space-between">
                    <Text>
                        #{ sale.id }
                    </Text>

                    <Popover opened={ popoverOpened } onChange={ setPopoverOpened } withArrow>
                        <Popover.Target>
                            <Button
                                variant="subtle" size="compact-md"
                                rightSection={ <IconArrowDown size={ 20 }/> }
                                onClick={ () => setPopoverOpened(!popoverOpened) }
                            >
                                { sale.deliveryAddress.split(',')[0] }
                            </Button>
                        </Popover.Target>

                        <Popover.Dropdown>
                            <Flex maw={ 250 }>
                                <Text>
                                    { sale.deliveryAddress }
                                </Text>
                            </Flex>
                        </Popover.Dropdown>
                    </Popover>

                    <Text>
                        £ { sale.total }
                    </Text>
                </Flex>
            </Card.Section>

            <Card.Section inheritPadding withBorder mt="sm">
                { sale.orderItems.map((orderItem, index) =>
                    <div key={ orderItem.id }>
                        <Flex w="100%" gap="sm" mb="xs">
                            <Flex align="center">
                                <Image
                                    src={ base64ToDataUri(orderItem.productImage) } alt="Product Image"
                                    fit="contain" h={ 100 } bg={ colorScheme === "dark" ? "dark.4" : "gray.2" }
                                    miw={ { base: 80, xs: 150 } } maw={ { base: 80, xs: 150 } }
                                />
                            </Flex>

                            <Flex direction="column" maw="55%">
                                <Text size="lg" fw={ 700 }>
                                    { orderItem.productName }
                                </Text>

                                <Text size="sm" mt="xs">
                                    Qty: { orderItem.productQuantity }
                                </Text>

                                <OrderItemStatusStepper status={ orderItem.status }/>

                                { orderItem.status === "PENDING_SHIPMENT" &&
                                    <Button
                                        variant="light" w="fit-content"
                                        mt="md" size="sm"
                                    >
                                        Mark as shipped
                                    </Button>
                                }
                            </Flex>

                            <Flex align="center" ms="auto">
                                <Text size="md">
                                    £ { orderItem.productPrice }
                                </Text>
                            </Flex>
                        </Flex>

                        { index !== (sale.orderItems.length - 1) &&
                            <Divider my="sm" size="md"/>
                        }
                    </div>
                ) }
            </Card.Section>
        </Card>
    );
}