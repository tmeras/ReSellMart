import { getWishlistQueryOptions } from "@/api/wishlist/getWishlist.ts";
import { api } from "@/lib/apiClient.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { z } from "zod";

export const CreateWishlistItemInputSchema = z.object({
    productId: z.number(),
    userId: z.number()
});

export type CreateWishlistItemInput = z.infer<typeof CreateWishlistItemInputSchema>;

export function createWishlistItem({ data }: { data: CreateWishlistItemInput }) {
    return api.post(`/api/users/${ data.userId }/wishlist/products`, data);
}

export type UseCreateWishlistItemOptions = {
    userId: number;
}

export function useCreateWishlistItem({ userId }: UseCreateWishlistItemOptions) {
    const queryClient = useQueryClient();

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getWishlistQueryOptions({ userId }).queryKey
            });
        },
        mutationFn: createWishlistItem
    });
}