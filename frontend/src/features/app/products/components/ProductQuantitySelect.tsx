import { useCreateCartItem } from "@/api/cart/createCartItem.ts";
import { useDeleteCartItem } from "@/api/cart/deleteCartItem.ts";
import { useUpdateCartItem } from "@/api/cart/updateCartItem.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { CartItemResponse, ProductResponse } from "@/types/api.tsx";
import { ActionIcon, Button, Flex, Select, Tooltip, useMantineColorScheme } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconTrash, IconX } from "@tabler/icons-react";
import { useEffect, useState } from "react";

export type ProductQuantitySelectProps = {
    product: ProductResponse;
    cartItem: CartItemResponse | undefined; // Used to determine if product is already in cart
    cartEnabled: boolean;
}

export function ProductQuantitySelect(
    { product, cartItem, cartEnabled }: ProductQuantitySelectProps
) {
    const { colorScheme } = useMantineColorScheme();
    const { user } = useAuth();
    const [quantity, setQuantity] = useState<string | null>("1");

    const createCartItemMutation = useCreateCartItem({ userId: user!.id });
    const updateCartItemMutation = useUpdateCartItem({ userId: user!.id });
    const deleteCartItemMutation = useDeleteCartItem({ userId: user!.id });

    useEffect(() => {
        if (cartEnabled && cartItem) {
            setQuantity(cartItem.quantity.toString());
        } else {
            setQuantity("1");
        }
    }, [cartEnabled, cartItem]);

    const quantityOptions =
        Array.from({ length: product.availableQuantity }, (_, index) => (index + 1).toString());

    async function addToCart() {
        try {
            await createCartItemMutation.mutateAsync({
                data: {
                    productId: product.id,
                    quantity: Number(quantity),
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

    async function updateQuantityInCart() {
        try {
            await updateCartItemMutation.mutateAsync({
                data: {
                    productId: product.id,
                    quantity: Number(quantity),
                    userId: user!.id
                }
            });
        } catch (error) {
            console.log("Error updating cart item", error);
            notifications.show({
                title: "Something went wrong", message: "Please try updating product quantity again",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    async function deleteFromCart() {
        try {
            await deleteCartItemMutation.mutateAsync({
                productId: product.id,
                userId: user!.id
            });
            setQuantity("1");
        } catch (error) {
            console.log("Error deleting cart item", error);
            notifications.show({
                title: "Something went wrong", message: "Please try removing the product from your cart again",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    if (!cartEnabled) {
        return (
            <Tooltip
                multiline w={ 250 }
                label="Cart could not be fetched. Please refresh and try again"
                events={ { hover: true, focus: false, touch: true } }
            >
                <Button
                    size="md" disabled
                    bg={ colorScheme === "dark" ? "dark.4" : "gray.2" }
                >
                    Add to cart
                </Button>
            </Tooltip>
        );
    }

    return (
        <Flex direction="column" gap="sm">
            <Select
                label="Quantity" w={ 200 }
                placeholder={ `Select quantity` }
                data={ quantityOptions }
                value={ quantity } onChange={ setQuantity }
            />

            <Flex gap="sm" align="center">
                <Button
                    size="sm"
                    onClick={ cartItem ? updateQuantityInCart : addToCart }
                    loading={ cartItem ? updateCartItemMutation.isPending : createCartItemMutation.isPending }
                >
                    { cartItem ? "Update cart quantity" : "Add to cart" }
                </Button>

                { cartItem &&
                    <ActionIcon
                        size="lg" variant="light"
                        onClick={ deleteFromCart } loading={ deleteCartItemMutation.isPending }
                    >
                        <IconTrash size={ 25 }/>
                    </ActionIcon>
                }
            </Flex>
        </Flex>
    );
}