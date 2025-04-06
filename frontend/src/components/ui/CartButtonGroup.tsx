import { useCreateCartItem } from "@/api/cart/createCartItem.ts";
import { useDeleteCartItem } from "@/api/cart/deleteCartItem.ts";
import { useUpdateCartItem } from "@/api/cart/updateCartItem.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { CartItemResponse, ProductResponse } from "@/types/api.tsx";
import { Button, useMantineTheme } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconMinus, IconPlus, IconX } from "@tabler/icons-react";

type CartButtonGroupProps = {
    cartItem: CartItemResponse | undefined; // Used to determine if product is already in cart
    product: ProductResponse;
}

export function CartButtonGroup({ cartItem, product }: CartButtonGroupProps) {
    const theme = useMantineTheme();
    const { user } = useAuth();

    const createCartItemMutation = useCreateCartItem({ userId: user!.id });
    const updateCartItemMutation = useUpdateCartItem({ userId: user!.id });
    const deleteCartItemMutation = useDeleteCartItem({ userId: user!.id });

    async function addToCart() {
        try {
            await createCartItemMutation.mutateAsync({
                data: {
                    productId: product.id,
                    quantity: 1,
                    userId: user!.id
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
                        productId: product.id,
                        quantity,
                        userId: user!.id
                    }
                });
            } else {
                await deleteCartItemMutation.mutateAsync({
                    productId: product.id,
                    userId: user!.id
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
            { cartItem ? (
                <Button.Group>
                    <Button variant="default" radius="md" size="compact-md"
                            onClick={ () => updateQuantityInCart(cartItem.quantity - 1) }
                    >
                        <IconMinus color={ theme.colors.red[6] } size={ 18 }/>
                    </Button>
                    <Button.GroupSection variant="default" size="compact-md" bg="var(--mantine-color-body)">
                        { cartItem.quantity }
                    </Button.GroupSection>
                    <Button
                        variant="default" radius="md" size="compact-md"
                        onClick={ () => updateQuantityInCart(cartItem.quantity + 1) }
                        disabled={ cartItem.quantity == product.availableQuantity }
                    >
                        <IconPlus color={ theme.colors.teal[6] } size={ 18 }/>
                    </Button>
                </Button.Group>
            ) : (
                <Button size="xs" onClick={ addToCart } loading={ createCartItemMutation.isPending }>
                    Add to cart
                </Button>
            ) }
        </>
    );
}