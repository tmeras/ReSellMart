import { getCartQueryOptions } from "@/api/cart/getCart.ts";
import { api } from "@/lib/apiClient.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { z } from "zod";

export const CreateCartItemInputSchema = z.object({
    productId: z.number(),
    quantity: z.number().positive("Cart item quantity must be a positive value"),
    userId: z.number()
});

export type CreateCartItemInput = z.infer<typeof CreateCartItemInputSchema>;

export function createCartItem({ data }: { data: CreateCartItemInput }) {
    return api.post(`/api/users/${ data.userId }/cart/products`, data);
}

export type UseCreateCartItemOptions = {
    userId: number
}

export function useCreateCartItem({ userId }: UseCreateCartItemOptions) {
    const queryClient = useQueryClient();

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getCartQueryOptions({ userId }).queryKey
            });
        },
        mutationFn: createCartItem
    });
}