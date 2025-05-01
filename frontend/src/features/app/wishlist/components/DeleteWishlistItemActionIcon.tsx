import { useDeleteWishlistItem } from "@/api/wishlist/deleteWishlistItem.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { ActionIcon } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconTrash, IconX } from "@tabler/icons-react";

type DeleteWishlistItemActionIconProps = {
    productId: string;
}

export function DeleteWishlistItemActionIcon({ productId }: DeleteWishlistItemActionIconProps) {
    const { user } = useAuth();

    const deleteWishlistItemMutation = useDeleteWishlistItem({ userId: user!.id.toString() });

    async function deleteWishlistItem() {
        try {
            await deleteWishlistItemMutation.mutateAsync({
                productId,
                userId: user!.id.toString()
            });
        } catch (error) {
            console.log("Error deleting wishlist item", error);
            notifications.show({
                title: "Something went wrong", message: "Please try adding the product to your wishlist again",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    return (
        <ActionIcon
            size="md" color="red.7" variant="subtle"
            loading={ deleteWishlistItemMutation.isPending } onClick={ deleteWishlistItem }
        >
            <IconTrash size={ 25 }/>
        </ActionIcon>
    );
}