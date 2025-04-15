import { CreateProductForm } from "@/features/app/products/components/CreateProductForm.tsx";
import { Flex, Title } from "@mantine/core";


export function CreateProductPage() {
    return (
        <>
            <title>{ `Create Product | ReSellMart` }</title>

            <Flex direction="column" justify="center" gap="md" mih="100vh">
                <Title ta="center">
                    Create Product Listing
                </Title>

                <CreateProductForm/>
            </Flex>
        </>
    );
}