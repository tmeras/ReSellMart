import { getProductsByUserQueryOptions } from "@/features/app/products/api/getProductsByUser.ts";
import { api } from "@/lib/apiClient.ts";
import { ProductResponse } from "@/types/api.ts";
import {
    MAX_PRODUCT_DESCRIPTION_LENGTH,
    MAX_PRODUCT_NAME_LENGTH,
    PRODUCT_CONDITION,
    ProductConditionKeys
} from "@/utils/constants.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { AxiosResponse } from "axios";
import { z } from "zod";

export const createProductInputSchema = z.object({
    name: z.string()
        .min(1, "Name is required")
        .max(MAX_PRODUCT_NAME_LENGTH, `Name must not exceed ${ MAX_PRODUCT_NAME_LENGTH } characters`),
    description: z.string()
        .min(1, "Description is required")
        .max(MAX_PRODUCT_DESCRIPTION_LENGTH, `Description must not exceed ${ MAX_PRODUCT_DESCRIPTION_LENGTH } characters`),
    price: z.number().positive("Price must be a positive number"),
    availableQuantity: z.number().positive("Available quantity must be a positive number"),
    condition: z.enum(Object.keys(PRODUCT_CONDITION) as [ProductConditionKeys]),
    categoryId: z.string().min(1, "Category is required")
});

export type CreateProductInput = z.infer<typeof createProductInputSchema>;

export function createProduct({ data }: { data: CreateProductInput }): Promise<AxiosResponse<ProductResponse>> {
    return api.post("/api/products", data);
}

export type UseCreateProductOptions = {
    sellerId: string;
};

export function useCreateProduct({ sellerId }: UseCreateProductOptions) {
    const queryClient = useQueryClient();
    //TODO: Invalidate admin queries here and elsewhere

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getProductsByUserQueryOptions({ userId: sellerId.toString() }).queryKey,
                exact: true
            });
        },
        mutationFn: createProduct
    });
}