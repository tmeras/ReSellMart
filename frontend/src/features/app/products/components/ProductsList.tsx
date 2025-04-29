import { useGetCart } from "@/api/cart/getCart.ts";
import { useGetWishlist } from "@/api/wishlist/getWishlist.ts";
import { ProductCard } from "@/features/app/products/components/ProductCard.tsx";
import { useAuth } from "@/hooks/useAuth.ts";
import { ProductResponse } from "@/types/api.ts";
import { Flex, Grid, Loader } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconX } from "@tabler/icons-react";
import { useEffect } from "react";

type ProductsListProps = {
    products: ProductResponse[]
}

export function ProductsList({ products }: ProductsListProps) {
    const { user } = useAuth();

    const userId = user!.id.toString();
    const getCartQuery = useGetCart({ userId });
    const getWishlistQuery = useGetWishlist({ userId });

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

    if (getCartQuery.isPending || getWishlistQuery.isPending) {
        return (
            <Flex align="center" justify="center" h="100vh">
                <Loader type="bars" size="md"/>
            </Flex>
        );
    }

    const cartItems = getCartQuery.data?.data;
    const wishlistItems = getWishlistQuery.data?.data;

    if (products.length === 0) return <div>No products to display</div>;

    const productCards = products.map((product: ProductResponse) => {
        // Determine if the product has been added to the cart
        const cartItem =
            cartItems?.find(cartItem => cartItem.product.id === product.id);

        // Determine if the product has been added to the wishlist
        const wishlistItem =
            wishlistItems?.find(wishlistItem => wishlistItem.product.id === product.id);

        return (
            <Grid.Col span={ { base: 12, sm: 6, md: 4, lg: 3 } } key={ product.id }>
                <Flex justify="center">
                    <ProductCard
                        product={ product }
                        cartItem={ cartItem } inWishlist={ !!wishlistItem }
                        cartEnabled={ getCartQuery.isSuccess } wishlistEnabled={ getWishlistQuery.isSuccess }
                    />
                </Flex>
            </Grid.Col>
        );
    });

    return (
        <Grid gutter="lg">
            { productCards }
        </Grid>
    );
}