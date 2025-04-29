import { useGetProductsByCategory } from "@/features/app/products/api/getProductsByCategory.ts";
import { ProductCard } from "@/features/app/products/components/ProductCard.tsx";
import { CartItemResponse, ProductResponse, WishlistItemResponse } from "@/types/api.ts";
import { shuffleArray } from "@/utils/generalUtils.ts";
import { Divider, Flex, Grid, Title } from "@mantine/core";
import { memo, useMemo } from "react";

export type SimilarProductsProps = {
    product: ProductResponse;
    wishlistItems: WishlistItemResponse[] | undefined; // Products in logged-in user's wishlist
    cartItems: CartItemResponse[] | undefined; // Products in logged-in user's cart
    wishlistEnabled: boolean;
    cartEnabled: boolean;
}

// Returns a grid of similar products based on the category of the given product
export const SimilarProducts = memo(function SimilarProducts(
    { product, wishlistItems, cartItems, wishlistEnabled, cartEnabled }: SimilarProductsProps
) {
    const getProductsByCategoryQuery = useGetProductsByCategory({
        categoryId: product.category.parentId ? product.category.parentId : product.category.id,
        page: 0
    });

    if (getProductsByCategoryQuery.isError) {
        console.log("Error fetching similar products", getProductsByCategoryQuery.error);
    }

    const similarProducts = getProductsByCategoryQuery.data?.data.content;

    // Exclude current product and shuffle when the data changes
    const shuffledSimilarProducts = useMemo(() => {
        if (!similarProducts) return [];

        const filteredProducts = similarProducts.filter(p => p.id !== product.id);

        const copy = [...filteredProducts];
        shuffleArray(copy);
        return copy;
    }, [similarProducts, product.id]);

    if (shuffledSimilarProducts.length === 0) return null;

    return (
        <>
            <Divider size="md" my="xl"/>

            <Title order={ 2 } my="lg" ta={ { base: "center", "md": "start" } }>
                Similar Products
            </Title>

            <Grid gutter="lg">
                { shuffledSimilarProducts.map((product: ProductResponse) => {
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
                                    cartEnabled={ cartEnabled } wishlistEnabled={ wishlistEnabled }
                                />
                            </Flex>
                        </Grid.Col>
                    );
                }) }
            </Grid>
        </>
    );
});