import { SalesList } from "@/features/app/orders/components/SalesList.tsx";
import { useAuth } from "@/hooks/useAuth.ts";
import { Title } from "@mantine/core";

export function SalesPage() {
    const { user } = useAuth();

    return (
        <>
            <title>{ `My sales | ReSellMart` }</title>

            <Title ta="center" mb="md">
                My Sales
            </Title>

            <SalesList userId={ user!.id.toString() }/>
        </>
    );
}