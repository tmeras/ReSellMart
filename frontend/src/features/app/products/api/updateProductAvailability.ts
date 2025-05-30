import { getProductsQueryOptions } from "@/features/app/products/api/getProducts.ts";
import { api } from "@/lib/apiClient.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";

export function updateProductAvailability({productId, data}: { productId: string, data: { isDeleted: boolean } }) {
    return api.patch(`/api/products/${productId}/availability`, data);
}

export function useUpdateProductAvailability() {
    const queryClient = useQueryClient();

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getProductsQueryOptions().queryKey
            });
        },
        mutationFn: updateProductAvailability
    });
}
