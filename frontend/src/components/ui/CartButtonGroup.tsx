import { useCreateCartItem } from "@/api/cart/createCartItem.ts";
import { useDeleteCartItem } from "@/api/cart/deleteCartItem.ts";
import { useUpdateCartItem } from "@/api/cart/updateCartItem.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { CartItemResponse, ProductResponse } from "@/types/api.ts";
import { Button, Tooltip, useMantineColorScheme, useMantineTheme } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconMinus, IconPlus, IconX } from "@tabler/icons-react";

type CartButtonGroupProps = {
    cartItem: CartItemResponse | undefined; // Used to determine if product is already in cart
    product: ProductResponse;
    cartEnabled: boolean
}

export function CartButtonGroup({ cartItem, product, cartEnabled }: CartButtonGroupProps) {
    const theme = useMantineTheme();
    const { colorScheme } = useMantineColorScheme();
    const { user } = useAuth();

    const userId = user!.id.toString();
    const createCartItemMutation = useCreateCartItem({ userId });
    const updateCartItemMutation = useUpdateCartItem({ userId });
    const deleteCartItemMutation = useDeleteCartItem({ userId });

    async function addToCart() {
        try {
            await createCartItemMutation.mutateAsync({
                data: {
                    productId: product.id.toString(),
                    quantity: 1,
                    userId
                }
            });
        } catch (error) {
            console.log("Error adding product to cart", error);
            notifications.show({
                title: "Something went wrong", message: "Please try adding the product to your cart again",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    async function updateQuantityInCart(quantity: number) {
        try {
            if (quantity > 0) {
                await updateCartItemMutation.mutateAsync({
                    data: {
                        productId: product.id.toString(),
                        quantity,
                        userId
                    }
                });
            } else {
                await deleteCartItemMutation.mutateAsync({
                    productId: product.id.toString(),
                    userId
                });
            }
        } catch (error) {
            console.log("Error updating cart item", error);
            notifications.show({
                title: "Something went wrong", message: "Please try updating product quantity again",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    return (
        <>
            { cartEnabled ? (
                cartItem ? (
                    <Button.Group>
                        <Button variant="default" radius="md" size="compact-md"
                                onClick={ () => updateQuantityInCart(cartItem.quantity - 1) }
                                loading={ updateCartItemMutation.isPending || deleteCartItemMutation.isPending }
                        >
                            <IconMinus color={ theme.colors.red[6] } size={ 18 }/>
                        </Button>
                        <Button.GroupSection variant="default" size="compact-md" bg="var(--mantine-color-body)">
                            { cartItem.quantity }
                        </Button.GroupSection>
                        <Button
                            variant="default" radius="md" size="compact-md"
                            onClick={ () => updateQuantityInCart(cartItem.quantity + 1) }
                            loading={ updateCartItemMutation.isPending }
                            disabled={ cartItem.quantity == product.availableQuantity }
                        >
                            <IconPlus color={ theme.colors.teal[6] } size={ 18 }/>
                        </Button>
                    </Button.Group>
                ) : (
                    <Button size="xs" onClick={ addToCart } loading={ createCartItemMutation.isPending }>
                        Add to cart
                    </Button>
                )
            ) : (
                <Tooltip
                    multiline w={ 250 }
                    label="Cart could not be fetched. Please refresh and try again"
                    events={ { hover: true, focus: false, touch: true } }
                >
                    <Button
                        size="xs" disabled
                        bg={ colorScheme === "dark" ? "dark.4" : "gray.2" }
                    >
                        Add to cart
                    </Button>
                </Tooltip>
            ) }
        </>
    );
}