import { getCartQueryOptions } from "@/api/cart/getCart.ts";
import { api } from "@/lib/apiClient.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";

export function deleteCartItem(
    { productId, userId }: { productId: string, userId: string }
) {
    return api.delete(`/api/users/${ userId }/cart/products/${ productId }`);
}

export type UseDeleteCartItemOptions = {
    userId: string;
};

export function useDeleteCartItem({ userId }: UseDeleteCartItemOptions) {
    const queryClient = useQueryClient();

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getCartQueryOptions({ userId }).queryKey
            });
        },
        mutationFn: deleteCartItem
    });
}