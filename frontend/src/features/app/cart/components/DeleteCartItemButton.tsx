import { useDeleteCartItem } from "@/api/cart/deleteCartItem.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { CartItemResponse } from "@/types/api.ts";
import { Button, ButtonProps } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconX } from "@tabler/icons-react";

type DeleteCartItemButtonProps = ButtonProps & {
    cartItem: CartItemResponse;
}

export function DeleteCartItemButton({ cartItem, ...rest }: DeleteCartItemButtonProps) {
    const { user } = useAuth();

    const deleteCartItemMutation = useDeleteCartItem({ userId: user!.id.toString() });

    const product = cartItem.product;

    async function deleteCartItem() {
        try {
            await deleteCartItemMutation.mutateAsync({
                productId: product.id.toString(),
                userId: user!.id.toString()
            });
        } catch (error) {
            console.error("Error deleting cart item", error);
            notifications.show({
                title: "Something went wrong", message: "Please try removing the product from your cart again",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    return (
        <Button
            size="compact-xs" variant="subtle" color="red.6"
            onClick={ deleteCartItem } loading={ deleteCartItemMutation.isPending }
            { ...rest }
        >
            Delete
        </Button>
    );
}