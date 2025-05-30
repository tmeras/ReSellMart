import { getCategoriesQueryOptions } from "@/api/categories/getCategories.ts";
import { api } from "@/lib/apiClient.ts";
import { MAX_CATEGORY_NAME_LENGTH } from "@/utils/constants.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { z } from "zod";

export const updateCategoryInputSchema = z.object({
    name: z.string()
        .min(1, "Name is required")
        .max(MAX_CATEGORY_NAME_LENGTH, `Name must not exceed ${ MAX_CATEGORY_NAME_LENGTH } characters`),
    parentId: z.string().min(1, "Parent category is required")
});

export type UpdateCategoryInput = z.infer<typeof updateCategoryInputSchema>;

export function updateCategory({ categoryId, data }: { categoryId: string, data: UpdateCategoryInput }) {
    return api.put(`/api/categories/${ categoryId }`, data);
}

export function useUpdateCategory() {
    const queryClient = useQueryClient();

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getCategoriesQueryOptions().queryKey
            });
        },
        mutationFn: updateCategory
    });
}