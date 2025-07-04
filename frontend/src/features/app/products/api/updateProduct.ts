import { getProductQueryOptions } from "@/features/app/products/api/getProduct.ts";
import { getProductsByUserQueryOptions } from "@/features/app/products/api/getProductsByUser.ts";
import { api } from "@/lib/apiClient.ts";
import { MAX_PRODUCT_DESCRIPTION_LENGTH, MAX_PRODUCT_NAME_LENGTH, PRODUCT_CONDITION } from "@/utils/constants.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { z } from "zod";

export const updateProductInputSchema = z.object({
    name: z.string()
        .min(1, "Name is required")
        .max(MAX_PRODUCT_NAME_LENGTH, `Name must not exceed ${ MAX_PRODUCT_NAME_LENGTH } characters`),
    description: z.string()
        .min(1, "Description is required")
        .max(MAX_PRODUCT_DESCRIPTION_LENGTH, `Description must not exceed ${ MAX_PRODUCT_DESCRIPTION_LENGTH } characters`),
    price: z.number().positive("Price must be a positive number"),
    availableQuantity: z.number().positive("Available quantity must be a positive number"),
    condition: z.enum(Object.keys(PRODUCT_CONDITION) as [keyof typeof PRODUCT_CONDITION]),
    categoryId: z.string().min(1, "Category is required")
});

export type UpdateProductInput = z.infer<typeof updateProductInputSchema>;

export function updateProduct({ productId, data }: { productId: string, data: UpdateProductInput }) {
    return api.patch(`/api/products/${ productId }`, data);
}

export type UseUpdateProductOptions = {
    productId: string;
    sellerId: string;
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