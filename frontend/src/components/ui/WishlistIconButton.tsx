import { useCreateWishlistItem } from "@/api/wishlist/createWishlistItem.ts";
import { useDeleteWishlistItem } from "@/api/wishlist/deleteWishlistItem.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { ActionIcon, useMantineTheme } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconHeart, IconHeartFilled, IconX } from "@tabler/icons-react";

type WishlistIconButtonProps = {
    inWishlist: boolean; // Used to determine if product is already in wishlist
    productId: number;
}

export function WishlistIconButton({ inWishlist, productId }: WishlistIconButtonProps) {
    const theme = useMantineTheme();
    const { user } = useAuth();
    const createWishlistItemMutation = useCreateWishlistItem({ userId: user!.id });
    const deleteWishlistItemMutation = useDeleteWishlistItem({ userId: user!.id });


    async function addToWishlist() {
        try {
            await createWishlistItemMutation.mutateAsync({
                data: {
                    productId,
                    userId: user!.id
                }
            });
        } catch (error) {
            console.log("Error saving wishlist item", error);
            notifications.show({
                title: "Something went wrong", message: "Please try adding the product to your wishlist again",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    async function deleteFromWishlist() {
        try {
            await deleteWishlistItemMutation.mutateAsync({
                productId,
                userId: user!.id
            });
        } catch (error) {
            console.log("Error deleting wishlist item", error);
            notifications.show({
                title: "Something went wrong", message: "Please try removing the product from your wishlist again ",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    return (
        <ActionIcon variant="subtle">
            { inWishlist ? (
                <IconHeartFilled
                    size={ 20 } color={ theme.colors.red[4] }
                    onClick={ deleteFromWishlist }
                />
            ) : (
                <IconHeart
                    size={ 20 } color={ theme.colors.red[4] }
                    onClick={ addToWishlist }
                />
            ) }
        </ActionIcon>
    );
}
