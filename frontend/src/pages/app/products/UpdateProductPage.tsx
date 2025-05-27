import { Authorisation } from "@/components/Authorisation.tsx";
import { useGetProduct } from "@/features/app/products/api/getProduct.ts";
import { UpdateProductForm } from "@/features/app/products/components/UpdateProductForm.tsx";
import { useAuth } from "@/hooks/useAuth.ts";
import { Flex, Loader, Text, Title } from "@mantine/core";
import { useParams } from "react-router";

export function UpdateProductPage() {
    const params = useParams();
    const productId = params.productId as string;

    const { user } = useAuth();

    const getProductQuery = useGetProduct({ productId });

    if (getProductQuery.isPending) {
        return (
            <Flex w="100%" h="100vh" align="center" justify="center">
                <Loader size="md"/>
            </Flex>
        );
    }

    if (getProductQuery.isError) {
        console.log("Product error", getProductQuery.error);
        return (
            <Text c="red.5">
                There was an error when fetching the product details. Please refresh and try again.
            </Text>
        );
    }

    const product = getProductQuery.data?.data;

    return (
        <>
            <title>{ `Update Product | ReSellMart` }</title>

            <Authorisation
                requiresAdminRole={ false }
                isAuthorised={ user!.id === product.seller.id }
                unauthorisedMessage="You are not allowed to modify products sold by other users."
            >
                <Title ta="center" mt="xl">
                    Update Product Listing
                </Title>

                <Flex justify="center" align="center" mih="100vh" mt="sm">
                    <UpdateProductForm product={ product }/>
                </Flex>
            </Authorisation>
        </>
    );
}