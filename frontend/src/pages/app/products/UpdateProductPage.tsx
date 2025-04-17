import { UpdateProductForm } from "@/features/app/products/components/UpdateProductForm.tsx";
import { Flex, Title } from "@mantine/core";

export function UpdateProductPage() {
    return (
        <>
            <title>{ `Update Product | ReSellMart` }</title>

            <Flex direction="column" justify="center" gap="md" mih="100vh">
                <Title ta="center">
                    Update Product Listing
                </Title>

                <UpdateProductForm/>
            </Flex>
        </>
    );
}