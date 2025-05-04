import { getCartQueryOptions } from "@/api/cart/getCart.ts";
import { api } from "@/lib/apiClient.ts";
import { CartItemResponse } from "@/types/api.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { AxiosResponse } from "axios";
import { z } from "zod";

export const CreateCartItemInputSchema = z.object({
    productId: z.string().min(1, "Product ID is required"),
    quantity: z.number().positive("Cart item quantity must be a positive value"),
    userId: z.string().min(1, "User ID is required")
});

export type CreateCartItemInput = z.infer<typeof CreateCartItemInputSchema>;

export function createCartItem(
    { data }: { data: CreateCartItemInput }
): Promise<AxiosResponse<CartItemResponse>> {
    return api.post(`/api/users/${ data.userId }/cart/products`, data);
}

export type UseCreateCartItemOptions = {
    userId: string;
};

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