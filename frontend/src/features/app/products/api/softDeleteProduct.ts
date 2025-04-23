import { getProductQueryOptions } from "@/features/app/products/api/getProduct.ts";
import { getProductsByUserQueryOptions } from "@/features/app/products/api/getProductsByUser.ts";
import { api } from "@/lib/apiClient.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";

export function softDeleteProduct({ productId }: { productId: string }) {
    return api.patch(`/api/products/${ productId }`, { isDeleted: true });
}

export type UseSoftDeleteProductOptions = {
    productId: string,
    sellerId: string
}

export function useSoftDeleteProduct({ productId, sellerId }: UseSoftDeleteProductOptions) {
    const queryClient = useQueryClient();

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getProductsByUserQueryOptions({ userId: sellerId }).queryKey
            });

            await queryClient.invalidateQueries({
                queryKey: getProductQueryOptions({ productId }).queryKey
            });
        },
        mutationFn: softDeleteProduct
    });
}