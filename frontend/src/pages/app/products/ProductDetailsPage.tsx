import { useGetCart } from "@/api/cart/getCart.ts";
import { useGetWishlist } from "@/api/wishlist/getWishlist.ts";
import { useGetProduct } from "@/features/app/products/api/getProduct.ts";
import { ProductDetails } from "@/features/app/products/components/ProductDetails.tsx";
import { SimilarProducts } from "@/features/app/products/components/SimilarProducts.tsx";
import { useAuth } from "@/hooks/useAuth.ts";
import { Flex, Loader, Text } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconX } from "@tabler/icons-react";
import axios from "axios";
import { useEffect } from "react";
import { useParams } from "react-router";

export function ProductDetailsPage() {
    const params = useParams();
    const productId = params.productId as string;

    const { user } = useAuth();

    const userId = user!.id.toString();
    const getProductQuery = useGetProduct({ productId });
    const getWishlistQuery = useGetWishlist({ userId });
    const getCartQuery = useGetCart({ userId });

    useEffect(() => {
        if (getWishlistQuery.isError) {
            console.log("Get wishlist error", getWishlistQuery.error);
            notifications.show({
                title: "Could not fetch wishlist", message: "Please refresh and try again",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }, [getWishlistQuery.isError, getWishlistQuery.error]);

    useEffect(() => {
        if (getCartQuery.isError) {
            console.log("Get cart error", getCartQuery.error);
            notifications.show({
                title: "Could not fetch cart", message: "Please refresh and try again",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }, [getCartQuery.isError, getCartQuery.error]);

    if (getProductQuery.isPending || getWishlistQuery.isPending || getCartQuery.isPending) {
        return (
            <Flex align="center" justify="center" h="100vh">
                <Loader type="bars" size="md"/>
            </Flex>
        );
    }

    const wishlistItems = getWishlistQuery.data?.data;
    const cartItems = getCartQuery.data?.data;

    if (getProductQuery.isError) {
        const error = getProductQuery.error;
        console.log("Error fetching product", error);

        if (axios.isAxiosError(error) && error.response?.status === 404) return (
            <Text c="red.5">
                The product could not be found.
            </Text>
        );

        return (
            <Text c="red.5">
                There was an error when fetching the product. Please refresh and try again.
            </Text>
        );
    }

    const product = getProductQuery.data?.data;

    // Determine if product is sold by logged-in user
    const isAuthUserProduct = product.seller.id === user!.id;

    return (
        <>
            <title>{ `${ product.name } | ReSellMart` }</title>

            <Flex direction="column" mih="100vh" w="100%" p="md">
                <ProductDetails
                    product={ product }
                    wishlistItems={ wishlistItems } cartItems={ cartItems }
                    wishlistEnabled={ getWishlistQuery.isSuccess } cartEnabled={ getCartQuery.isSuccess }
                />

                { !isAuthUserProduct &&
                    <SimilarProducts
                        product={ product }
                        wishlistItems={ wishlistItems } cartItems={ cartItems }
                        wishlistEnabled={ getWishlistQuery.isSuccess } cartEnabled={ getCartQuery.isSuccess }
                    />
                }
            </Flex>
        </>
    );
}