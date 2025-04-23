import { paths } from "@/config/paths.ts";
import { SellerProductsList } from "@/features/app/products/components/SellerProductsList.tsx";
import { useAuth } from "@/hooks/useAuth.ts";
import { Button, Flex, Title } from "@mantine/core";
import { IconCirclePlus } from "@tabler/icons-react";
import { Link } from "react-router";

export function SellerProductsPage() {
    const { user } = useAuth();

    return (
        <>
            <title>{ `My Products | ReSellMart` }</title>

            <Title ta="center" mt="lg">
                My Products
            </Title>

            <Flex direction="column" justify="center" align="center">
                <Flex
                    justify="flex-end" mb="md"
                    w={ { base: 480, xs: 550, md: 800 } }
                >
                    <Button
                        size="sm" variant="light" mt="sm" ms="sm"
                        w="fit-content"
                        leftSection={ <IconCirclePlus size={ 18 }/> }
                        component={ Link } to={ paths.app.createProduct.path }
                    >
                        Create product listing
                    </Button>
                </Flex>
                <SellerProductsList sellerId={ user!.id.toString() }/>
            </Flex>
        </>
    );
}