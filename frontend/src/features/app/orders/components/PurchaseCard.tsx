import { CustomLink } from "@/components/ui/link/CustomLink.tsx";
import { paths } from "@/config/paths.ts";
import { OrderItemStatusStepper } from "@/features/app/orders/components/OrderItemStatusStepper.tsx";
import { OrderResponse } from "@/types/api.ts";
import { PRODUCT_CONDITION } from "@/utils/constants.ts";
import { base64ToDataUri } from "@/utils/fileUtils.ts";
import { Button, Card, Divider, Flex, Image, Popover, Text, useMantineColorScheme } from "@mantine/core";
import { IconArrowDown } from "@tabler/icons-react";
import { useState } from "react";

export type PurchaseCardProps = {
    purchase: OrderResponse;
}

export function PurchaseCard({ purchase }: PurchaseCardProps) {
    const { colorScheme } = useMantineColorScheme();
    const [popoverOpened, setPopoverOpened] = useState(false);

    const placedAt = new Date(purchase.placedAt);

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
                        #{ purchase.id }
                    </Text>

                    <Popover opened={ popoverOpened } onChange={ setPopoverOpened } withArrow>
                        <Popover.Target>
                            <Button
                                variant="subtle" size="compact-md"
                                rightSection={ <IconArrowDown size={ 20 }/> }
                                onClick={ () => setPopoverOpened(!popoverOpened) }
                            >
                                { purchase.deliveryAddress.split(',')[0] }
                            </Button>
                        </Popover.Target>

                        <Popover.Dropdown>
                            <Flex maw={ 250 }>
                                <Text>
                                    { purchase.deliveryAddress }
                                </Text>
                            </Flex>
                        </Popover.Dropdown>
                    </Popover>

                    <Text>
                        £ { purchase.total }
                    </Text>
                </Flex>
            </Card.Section>

            <Card.Section inheritPadding withBorder mt="sm">
                { purchase.orderItems.map((orderItem, index) =>
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

                                <Text size="xs" c="dimmed">
                                    Sold by { " " }
                                    <CustomLink
                                        to={ paths.app.productsByUser.getHref(orderItem.productSeller.id.toString()) }>
                                        { orderItem.productSeller.name }
                                    </CustomLink>
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

                                { orderItem.status === "SHIPPED" &&
                                    <Button
                                        variant="light" w="fit-content"
                                        mt="md" size="sm"
                                    >
                                        Mark as delivered
                                    </Button>
                                }
                            </Flex>


                            <Flex align="center" ms="auto">
                                <Text size="md">
                                    £ { orderItem.productPrice }
                                </Text>
                            </Flex>
                        </Flex>

                        { index !== (purchase.orderItems.length - 1) &&
                            <Divider my="sm" size="md"/>
                        }
                    </div>
                ) }
            </Card.Section>
        </Card>
    );
}