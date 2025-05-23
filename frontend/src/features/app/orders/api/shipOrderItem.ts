import { getSalesByUser, getSalesByUserQueryOptions } from "@/features/app/orders/api/getSalesByUser.ts";
import { api } from "@/lib/apiClient.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";

export function shipOrderItem({orderId, productId} : {orderId: string, productId: string}) {
  return api.patch(`/api/orders/${orderId}/products/${productId}/ship`)
}

export type UseShipOrderItemOptions = {
    userId: string;
}

export function useShipOrderItem({userId}: UseShipOrderItemOptions) {
    const queryClient = useQueryClient();

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getSalesByUserQueryOptions({userId}).queryKey
            });
        },
        mutationFn: shipOrderItem
    });
}