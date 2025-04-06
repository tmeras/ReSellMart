import { getCartQueryOptions } from "@/api/cart/getCart.ts";
import { api } from "@/lib/apiClient.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { z } from "zod";

export const UpdateCartItemInputSchema = z.object({
    productId: z.number(),
    quantity: z.number().positive("Cart item quantity must be a positive value"),
    userId: z.number()
});

export type UpdateCartItemInput = z.infer<typeof UpdateCartItemInputSchema>;

export function updateCartItem(
    { data }: { data: UpdateCartItemInput }
) {
    return api.patch(`/api/users/${ data.userId }/cart/products/${ data.productId }`, data);
}

type UseUpdateCartItemOptions = {
    userId: number
}

export function useUpdateCartItem({ userId }: UseUpdateCartItemOptions) {
    const queryClient = useQueryClient();

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getCartQueryOptions({ userId }).queryKey
            });
        },
        mutationFn: updateCartItem
    });
}