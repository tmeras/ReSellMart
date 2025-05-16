import { getCartQueryOptions } from "@/api/cart/getCart.ts";
import { getPurchasesByUserQueryOptions } from "@/features/app/orders/api/getPurchasesByUser.ts";
import { api } from "@/lib/apiClient.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { z } from "zod";

export const createOrderInputSchema = z.object({
    billingAddressId: z.string().min(1, "Billing address ID is required"),
    deliveryAddressId: z.string().min(1, "Delivery address ID is required")
});

export type CreateOrderInput = z.infer<typeof createOrderInputSchema>;

export function createOrder({ data }: { data: CreateOrderInput }) {
    return api.post("/api/orders", data);
}

export type UseCreateOrderOptions = {
    userId: string;
}

export function useCreateOrder({ userId }: UseCreateOrderOptions) {
    const queryClient = useQueryClient();
    // TODO: Invalidate admin order queries (also in other products queries etc.)

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getCartQueryOptions({ userId }).queryKey
            });

            await queryClient.invalidateQueries({
                queryKey: getPurchasesByUserQueryOptions({ userId }).queryKey
            });
        },
        mutationFn: createOrder
    });
}