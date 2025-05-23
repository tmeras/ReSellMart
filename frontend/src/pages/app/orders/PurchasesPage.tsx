import { PurchasesList } from "@/features/app/orders/components/PurchasesList.tsx";
import { useAuth } from "@/hooks/useAuth.ts";
import { Title } from "@mantine/core";

export function PurchasesPage() {
    const { user } = useAuth();

    return (
        <>
            <title>{ `My purchases | ReSellMart` }</title>

            <Title ta="center" mb="md">
                My Purchases
            </Title>

            <PurchasesList userId={ user!.id.toString() }/>
        </>
    );
}