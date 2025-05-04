import { getWishlistQueryOptions } from "@/api/wishlist/getWishlist.ts";
import { api } from "@/lib/apiClient.ts";
import { WishlistItemResponse } from "@/types/api.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { AxiosResponse } from "axios";
import { z } from "zod";

export const CreateWishlistItemInputSchema = z.object({
    productId: z.string().min(1, "Product ID is required"),
    userId: z.string().min(1, "User ID is required")
});

export type CreateWishlistItemInput = z.infer<typeof CreateWishlistItemInputSchema>;

export function createWishlistItem(
    { data }: { data: CreateWishlistItemInput }
): Promise<AxiosResponse<WishlistItemResponse>> {
    return api.post(`/api/users/${ data.userId }/wishlist/products`, data);
}

export type UseCreateWishlistItemOptions = {
    userId: string;
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