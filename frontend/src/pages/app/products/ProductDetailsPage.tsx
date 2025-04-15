import { useGetCart } from "@/api/cart/getCart.ts";
import { useGetWishlist } from "@/api/wishlist/getWishlist.ts";
import { useGetProduct } from "@/features/app/products/api/getProduct.ts";
import { ProductDetails } from "@/features/app/products/components/ProductDetails.tsx";
import { SimilarProducts } from "@/features/app/products/components/SimilarProducts.tsx";
import { useAuth } from "@/hooks/useAuth.ts";
import { Divider, Flex, Loader, Text } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconX } from "@tabler/icons-react";
import { useEffect } from "react";
import { useParams } from "react-router";

export function ProductDetailsPage() {
    const params = useParams();
    const productId = params.productId as string;

    const { user } = useAuth();

    const getProductQuery = useGetProduct({ productId });
    const getWishlistQuery = useGetWishlist({ userId: user!.id });
    const getCartQuery = useGetCart({ userId: user!.id });

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

    if (getProductQuery.isLoading || getWishlistQuery.isLoading || getCartQuery.isLoading) {
        return (
            <Flex align="center" justify="center" h="100vh">
                <Loader type="bars" size="md"/>
            </Flex>
        );
    }

    const product = getProductQuery.data?.data;
    const wishlistItems = getWishlistQuery.data?.data;
    const cartItems = getCartQuery.data?.data;

    if (getProductQuery.isError || !product) {
        console.log("Error fetching product", getProductQuery.error);
        return (
            <Text c="red.5">
                There was an error when fetching the product. Please refresh and try again.
            </Text>
        );
    }

    return (
        <>
            <title>{ `${ product.name } | ReSellMart` }</title>

            <Flex direction="column" mih="100vh" w="100%" p="md">
                <ProductDetails
                    product={ product }
                    wishlistItems={ wishlistItems } cartItems={ cartItems }
                    wishlistEnabled={ getWishlistQuery.isSuccess } cartEnabled={ getCartQuery.isSuccess }
                />

                <Divider size="md" my="xl"/>

                <SimilarProducts
                    product={ product }
                    wishlistItems={ wishlistItems } cartItems={ cartItems }
                    wishlistEnabled={ getWishlistQuery.isSuccess } cartEnabled={ getCartQuery.isSuccess }
                />
            </Flex>
        </>
    );
}