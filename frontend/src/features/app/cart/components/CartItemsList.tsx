import { CartItem } from "@/features/app/cart/components/CartItem.tsx";
import { CartItemResponse } from "@/types/api.ts";
import { Alert, Divider, Flex, Paper, Text, Title } from "@mantine/core";
import { IconInfoCircle } from "@tabler/icons-react";
import { ReactNode } from "react";

type CartItemsListProps = {
    cartItems: CartItemResponse[];
    cartTotal: number;
    totalItems: number;
    cartValid: boolean;
    cartEmpty: boolean;
    title?: ReactNode
}

export function CartItemsList({
                                  cartItems, cartTotal, totalItems, cartValid, cartEmpty, title = <Title>Cart</Title>
}: CartItemsListProps) {
    return (
        <Paper
            p="md" radius="sm"
            withBorder shadow="lg"
            w={ { base: 480, xs: 550, md: 650 } }
        >
            { !cartEmpty && !cartValid &&
                <Alert
                    title="Cart requires adjustment" mb="sm"
                    color="red.6" variant="light" icon={ <IconInfoCircle/> }
                >
                    Your cart includes unavailable products or
                    the requested quantities of some products exceed available stock
                </Alert>
            }

            <Flex align="center" gap="xs">
                { title }

                <Text size="xl" mt={ 5 }>
                    ({ totalItems } items)
                </Text>
            </Flex>

            <Flex justify="flex-end">
                <Text size="md">
                    Price
                </Text>
            </Flex>

            <Divider my="xs"/>

            { cartEmpty ? (
                <Flex align="center" justify="center">
                    <Text size="xl">
                        Your cart is empty...
                    </Text>
                </Flex>
            ) : (
                <>
                    <Flex direction="column">
                        { cartItems.map((item, index) =>
                            <div key={ item.id }>
                                <CartItem cartItem={ item }/>

                                { index !== (cartItems.length - 1) &&
                                    <Flex p="sm" direction="column">
                                        <Divider/>
                                    </Flex>
                                }
                            </div>
                        ) }
                    </Flex>

                    <Divider my="xs"/>

                    <Flex justify="flex-end">
                        <Text size="md">
                            Total: £ { cartTotal }
                        </Text>
                    </Flex>
                </>
            ) }
        </Paper>
    );
}