import { useShipOrderItem } from "@/features/app/orders/api/shipOrderItem.ts";
import { OrderItemStatusStepper } from "@/features/app/orders/components/OrderItemStatusStepper.tsx";
import { useAuth } from "@/hooks/useAuth.ts";
import { OrderResponse } from "@/types/api.ts";
import { PRODUCT_CONDITION } from "@/utils/constants.ts";
import { base64ToDataUri } from "@/utils/fileUtils.ts";
import { Button, Card, Divider, Flex, Image, Popover, Text, useMantineColorScheme } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconArrowDown, IconX } from "@tabler/icons-react";
import { useState } from "react";

export type SaleCardProps = {
    sale: OrderResponse;
}

export function SaleCard({ sale }: SaleCardProps) {
    const {user} = useAuth();
    const { colorScheme } = useMantineColorScheme();
    const [popoverOpened, setPopoverOpened] = useState(false);

    const shipOrderItemMutation = useShipOrderItem({userId: user!.id.toString()});

    async function shipOrderItem(orderId: string, productId: string) {
        try {
            await shipOrderItemMutation.mutateAsync({ orderId, productId });
        } catch (error) {
            console.error("Error marking order item as shipped:", error);
            notifications.show({
                title: "Something went wrong", message: "Please try marking the product as shipped again",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

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

                                <Flex gap={ 5 }>
                                    <Text size="sm" mt="xs">
                                        Qty: { orderItem.productQuantity },
                                    </Text>

                                    <Text size="sm" mt="xs">
                                        Condition: {PRODUCT_CONDITION[orderItem.productCondition] }
                                    </Text>
                                </Flex>

                                <OrderItemStatusStepper status={ orderItem.status }/>

                                { orderItem.status === "PENDING_SHIPMENT" &&
                                    <Button
                                        variant="light" w="fit-content"
                                        mt="md" size="sm"
                                        loading={shipOrderItemMutation.isPending}
                                        onClick={() =>
                                            shipOrderItem(sale.id.toString(), orderItem.productId.toString())
                                        }
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