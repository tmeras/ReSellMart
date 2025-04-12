import { useGetProduct } from "@/features/app/products/api/getProduct.ts";
import { ProductDetails } from "@/features/app/products/components/ProductDetails.tsx";
import { Flex, Loader, Text } from "@mantine/core";
import { useParams } from "react-router";

export function ProductDetailsPage() {
    const params = useParams();
    const productId = params.productId as string;

    const getProductQuery = useGetProduct({ productId });

    if (getProductQuery.isLoading) {
        return (
            <Flex align="center" justify="center" h="100vh">
                <Loader type="bars" size="md"/>
            </Flex>
        );
    }

    const product = getProductQuery.data?.data;

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

            <ProductDetails product={ product }/>
        </>
    );
}