import { useCreateWishlistItem } from "@/api/wishlist/createWishlistItem.ts";
import { useDeleteWishlistItem } from "@/api/wishlist/deleteWishlistItem.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { ActionIcon, Tooltip, useMantineColorScheme, useMantineTheme } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconHeart, IconHeartFilled, IconX } from "@tabler/icons-react";

type WishlistIconButtonProps = {
    inWishlist: boolean; // Used to determine if product is already in wishlist
    productId: number;
    wishlistEnabled: boolean;
    size: number;
}

export function WishlistIconButton(
    { inWishlist, productId, wishlistEnabled, size }: WishlistIconButtonProps
) {
    const theme = useMantineTheme();
    const { colorScheme } = useMantineColorScheme();
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
        wishlistEnabled ? (
            <ActionIcon
                variant="subtle"
                loading={ createWishlistItemMutation.isPending || deleteWishlistItemMutation.isPending }
            >
                { inWishlist ? (
                    <IconHeartFilled
                        size={ size } color={ theme.colors.red[4] }
                        onClick={ deleteFromWishlist }
                    />
                ) : (
                    <IconHeart
                        size={ size } color={ theme.colors.red[4] }
                        onClick={ addToWishlist }
                    />
                ) }
            </ActionIcon>
        ) : (
            <Tooltip
                multiline w={ 250 }
                label="Wishlist could not be fetched. Please refresh and try again."
                events={ { hover: true, focus: false, touch: true } }
            >
                <ActionIcon
                    variant="subtle" disabled
                    bg={ colorScheme === "dark" ? "dark.4" : "gray.2" }
                >
                    <IconHeart size={ size }/>
                </ActionIcon>
            </Tooltip>
        )
    );
}
