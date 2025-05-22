import { getPurchasesByUserQueryOptions } from "@/features/app/orders/api/getPurchasesByUser.ts";
import { getSalesByUserQueryOptions } from "@/features/app/orders/api/getSalesByUser.ts";
import { api } from "@/lib/apiClient.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";

export function deliverOrderItem({orderId, productId} : {orderId: string, productId: string}) {
    return api.patch(`/api/orders/${orderId}/products/${productId}/deliver`)
}

export type UseDeliverOrderItemOptions = {
    userId: string;
}

export function useDeliverOrderItem({userId}: UseDeliverOrderItemOptions) {
    const queryClient = useQueryClient();

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getPurchasesByUserQueryOptions({userId}).queryKey
            });
        },
        mutationFn: deliverOrderItem
    });
}