import { getProductQueryOptions } from "@/features/app/products/api/getProduct.ts";
import { getProductsByUserQueryOptions } from "@/features/app/products/api/getProductsByUser.ts";
import { api } from "@/lib/apiClient.ts";
import { PRODUCT_CONDITION } from "@/utils/constants.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { z } from "zod";

export const updateProductInputSchema = z.object({
    name: z.string().min(1, "Name must not be empty"),
    description: z.string().min(1, "Description must not be empty"),
    price: z.number().positive("Price must be a positive number"),
    availableQuantity: z.number().positive("Available quantity must be a positive number"),
    productCondition: z.enum(Object.keys(PRODUCT_CONDITION) as [keyof typeof PRODUCT_CONDITION]),
    categoryId: z.string().min(1, "Category must not be empty")
});

export type UpdateProductInput = z.infer<typeof updateProductInputSchema>;

export function updateProduct({ productId, data }: { productId: string, data: UpdateProductInput }) {
    return api.patch(`/api/products/${ productId }`, data);
}

export type UseUpdateProductOptions = {
    productId: string,
    sellerId: number
}

export function useUpdateProduct({ productId, sellerId }: UseUpdateProductOptions) {
    const queryClient = useQueryClient();

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getProductsByUserQueryOptions({ userId: sellerId.toString() }).queryKey,
                exact: true
            });

            await queryClient.invalidateQueries({
                queryKey: getProductQueryOptions({ productId }).queryKey,
                exact: true
            });
        },
        mutationFn: updateProduct
    });
}