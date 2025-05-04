import { getCartQueryOptions } from "@/api/cart/getCart.ts";
import { api } from "@/lib/apiClient.ts";
import { CartItemResponse } from "@/types/api.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { AxiosResponse } from "axios";
import { z } from "zod";

export const UpdateCartItemInputSchema = z.object({
    productId: z.string().min(1, "Product ID is required"),
    quantity: z.number().positive("Cart item quantity must be a positive value"),
    userId: z.string().min(1, "User ID is required")
});

export type UpdateCartItemInput = z.infer<typeof UpdateCartItemInputSchema>;

export function updateCartItem(
    { data }: { data: UpdateCartItemInput }
): Promise<AxiosResponse<CartItemResponse>> {
    return api.patch(`/api/users/${ data.userId }/cart/products/${ data.productId }`, data);
}

export type UseUpdateCartItemOptions = {
    userId: string;
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