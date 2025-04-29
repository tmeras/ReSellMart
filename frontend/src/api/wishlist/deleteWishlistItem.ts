import { getWishlistQueryOptions } from "@/api/wishlist/getWishlist.ts";
import { api } from "@/lib/apiClient.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";

export function deleteWishlistItem(
    { productId, userId }: { productId: string, userId: string }
) {
    return api.delete(`/api/users/${ userId }/wishlist/products/${ productId }`);
}

export type UseDeleteWishlistItemOptions = {
    userId: string;
};

export function useDeleteWishlistItem({ userId }: UseDeleteWishlistItemOptions) {
    const queryClient = useQueryClient();

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getWishlistQueryOptions({ userId }).queryKey
            });
        },
        mutationFn: deleteWishlistItem
    });
}