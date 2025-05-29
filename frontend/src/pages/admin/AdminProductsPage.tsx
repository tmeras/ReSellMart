import { Authorisation } from "@/components/Authorisation.tsx";
import { ProductsTable } from "@/features/app/products/components/ProductsTable.tsx";
import { Title } from "@mantine/core";

export function AdminProductsPage() {

    return (
        <>
            <Authorisation requiresAdminRole={ true }>
                <title>{ `Manage Products | ReSellMart` }</title>

                <Title ta="center" mb="md">
                    Products
                </Title>

                <ProductsTable />
            </Authorisation>
        </>
    );
}