import { useGetCart } from "@/api/cart/getCart.ts";
import { useGetWishlist } from "@/api/wishlist/getWishlist.ts";
import { WishlistItemCard } from "@/features/app/wishlist/components/WishlistItemCard.tsx";
import { useAuth } from "@/hooks/useAuth.ts";
import { Flex, Loader, Text } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconX } from "@tabler/icons-react";
import { useEffect } from "react";

export function WishlistItemsList() {
    const { user } = useAuth();

    const getWishlistQuery = useGetWishlist({ userId: user!.id.toString() });
    const getCartQuery = useGetCart({ userId: user!.id.toString() });

    useEffect(() => {
        if (getCartQuery.isError) {
            console.log("Get cart error", getCartQuery.error);
            notifications.show({
                title: "Could not fetch cart", message: "Please refresh and try again",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }, [getCartQuery.isError, getCartQuery.error]);

    if (getWishlistQuery.isPending || getCartQuery.isPending) {
        return (
            <Flex align="center" justify="center" h="100vh">
                <Loader type="bars" size="md"/>
            </Flex>
        );
    }

    if (getWishlistQuery.isError) {
        console.log("Error fetching wishlist", getWishlistQuery.error);
        return (
            <Text c="red.5">
                There was an error when fetching your wishlist. Please refresh and try again.
            </Text>
        );
    }

    const wishlistItems = getWishlistQuery.data?.data;
    const cartItems = getCartQuery.data?.data;

    if (wishlistItems.length === 0) return (
        <Flex align="center" justify="center" h="60vh">
            You have no items in your wishlist...
        </Flex>
    );

    const wishListItemCards = wishlistItems?.map((wishlistItem) => {
        // Determine if the product has been added to the cart
        const cartItem =
            cartItems?.find((cartItem) => cartItem.product.id === wishlistItem.product.id);

        return (
            <WishlistItemCard
                key={ wishlistItem.id } wishlistItem={ wishlistItem }
                cartItem={ cartItem } cartEnabled={ getCartQuery.isSuccess }
            />
        );
    });

    return (
        <Flex direction="column" gap="xl">
            { wishListItemCards }
        </Flex>
    );
}