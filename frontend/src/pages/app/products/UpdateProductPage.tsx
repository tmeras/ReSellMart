import { UpdateProductForm } from "@/features/app/products/components/UpdateProductForm.tsx";
import { Flex, Title } from "@mantine/core";

export function UpdateProductPage() {
    return (
        <>
            <title>{ `Update Product | ReSellMart` }</title>

            <Title ta="center" mt="xl">
                Update Product Listing
            </Title>

            <Flex justify="center" align="center" mih="100vh" mt="sm">
                <UpdateProductForm/>
            </Flex>
        </>
    );
}