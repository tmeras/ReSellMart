import { CreateProductForm } from "@/features/app/products/components/CreateProductForm.tsx";
import { Flex, Title } from "@mantine/core";


export function CreateProductPage() {
    return (
        <>
            <title>{ `Create Product | ReSellMart` }</title>

            <Title ta="center" mt="xl">
                Create Product Listing
            </Title>

            <Flex justify="center" align="center" mih="100vh">
                <CreateProductForm/>
            </Flex>
        </>
    );
}