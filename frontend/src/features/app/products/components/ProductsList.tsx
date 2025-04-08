import { useGetCart } from "@/api/cart/getCart.ts";
import { useGetWishlist } from "@/api/wishlist/getWishlist.ts";
import { ProductCard } from "@/features/app/products/components/ProductCard.tsx";
import { useAuth } from "@/hooks/useAuth.ts";
import { ProductResponse } from "@/types/api.tsx";
import { Flex, Grid, Loader } from "@mantine/core";

type ProductsListProps = {
    products: ProductResponse[]
}

export function ProductsList({ products }: ProductsListProps) {
    const { user } = useAuth();
    const getCartQuery = useGetCart({ userId: user!.id });
    const getWishlistQuery = useGetWishlist({ userId: user!.id });

    if (getCartQuery.isLoading || getWishlistQuery.isLoading) {
        return (
            <Flex align="center" justify="center" h="100vh">
                <Loader type="bars" size="md"/>
            </Flex>
        );
    }

    const cartItems = getCartQuery.data?.data;

    const wishlistItems = getWishlistQuery.data?.data;

    if (getCartQuery.isError || !cartItems) {
        console.log("Get cart error", getCartQuery.error);
        return <div>There was an error when fetching your cart. Please try again later</div>;
    }

    if (getWishlistQuery.isError || !wishlistItems) {
        console.log("Get wishlist error", getWishlistQuery.error);
        return <div>There was an error when fetching your wishlist. Please try again later</div>;
    }

    if (products.length === 0) return <div>No products to display</div>;

    const productCards = products.map((product: ProductResponse) => {
        // Determine if the product has been added to the cart
        const cartItem =
            cartItems.find(cartItem => cartItem.product.id === product.id);

        // Determine if the product has been added to the wishlist
        const wishlistItem =
            wishlistItems.find(wishlistItem => wishlistItem.product.id === product.id);

        return (
            <Grid.Col span={ { base: 12, sm: 6, md: 4, lg: 3 } } key={ product.id }>
                <Flex justify="center">
                    <ProductCard product={ product } cartItem={ cartItem } inWishlist={ !!wishlistItem }/>
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