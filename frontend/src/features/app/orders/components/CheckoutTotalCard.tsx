import { useCreateOrder } from "@/features/app/orders/api/createOrder.ts";
import { useAuth } from "@/hooks/useAuth";
import { AddressResponse } from "@/types/api.ts";
import { Button, Divider, Flex, Paper, Text, Tooltip } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconX } from "@tabler/icons-react";

export type CheckoutTotalCardProps = {
    deliveryAddress: AddressResponse | undefined;
    billingAddress: AddressResponse | undefined;
    cartTotal: number;
    totalItems: number;
    cartValid: boolean;
    setLoading: (loading: boolean) => void;
}

export function CheckoutTotalCard(
    { deliveryAddress, billingAddress, cartTotal, totalItems, cartValid, setLoading }: CheckoutTotalCardProps
) {
    const { user } = useAuth();

    const createOrderMutation = useCreateOrder({ userId: user!.id.toString() });

    async function placeOrder() {
        if (!deliveryAddress || !billingAddress) return;

        try {
            setLoading(true);
            const response = await createOrderMutation.mutateAsync({
                data: {
                    billingAddressId: billingAddress.id.toString(),
                    deliveryAddressId: deliveryAddress.id.toString()
                }
            });

            // Navigate to Stripe checkout page
            window.location.href = response.data.redirectUrl;
        } catch (error) {
            console.log("Error creating order", error);
            notifications.show({
                title: "Something went wrong", message: "Please try proceeding to payment again",
                color: "red", icon: <IconX/>, withBorder: true
            });
            setLoading(false);
        }
    }

    return (
        <Paper
            p="md" radius="sm" miw={ 300 } maw={ 400 }
            withBorder shadow="lg" h="fit-content"
            mt={ { base: "0", md: "50" } }
            style={ { position: "sticky", top: 80 } }
        >
            <Flex gap="sm">
                <Text size="lg" fw={ 700 }>
                    Total ({ totalItems } items):
                </Text>
                <Text mt={ 3 }>
                    £ { cartTotal }
                </Text>
            </Flex>

            <Divider my="xs"/>

            { deliveryAddress &&
                <Flex gap="sm" mt="sm">
                    <Text size="lg" fw={ 700 } miw="35%">
                        Delivering to:
                    </Text>
                    <Text mt={ 3 }>
                        { `
                            ${ deliveryAddress.name }, ${ deliveryAddress.street }, 
                            ${ deliveryAddress.state }, ${ deliveryAddress.city }, 
                            ${ deliveryAddress.postalCode }, ${ deliveryAddress.country } 
                            ${ deliveryAddress.phoneNumber ? `, ${ deliveryAddress.phoneNumber }` : '' }
                          `
                        }

                    </Text>
                </Flex>
            }

            { billingAddress &&
                <Flex gap="sm" mt="sm">
                    <Text size="lg" fw={ 700 } miw="35%">
                        Billing:
                    </Text>
                    <Text mt={ 3 }>
                        { `
                            ${ billingAddress.name }, ${ billingAddress.street }, 
                            ${ billingAddress.state }, ${ billingAddress.city }, 
                            ${ billingAddress.postalCode }, ${ billingAddress.country } 
                            ${ billingAddress.phoneNumber ? `, ${ billingAddress.phoneNumber }` : '' }
                          `
                        }
                    </Text>
                </Flex>
            }

            <Divider my="xs"/>

            { (cartValid && deliveryAddress && billingAddress) ? (
                <Button
                    fullWidth mt="md"
                    onClick={ placeOrder }
                >
                    Proceed to payment
                </Button>
            ) : (
                <Tooltip
                    events={ { hover: true, focus: false, touch: true } }
                    position="bottom" multiline w={ 350 }
                    label="Cannot complete checkout. Your cart requires adjusting or you haven't
                        specified either a delivery address or billing addresss"
                >
                    <Button fullWidth mt="md" disabled>
                        Proceed to payment
                    </Button>
                </Tooltip>
            ) }
        </Paper>
    );
}