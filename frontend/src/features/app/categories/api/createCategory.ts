import { getCategoriesQueryOptions } from "@/api/categories/getCategories.ts";
import { api } from "@/lib/apiClient.ts";
import { MAX_CATEGORY_NAME_LENGTH } from "@/utils/constants.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { z } from "zod";

export const createCategoryInputSchema = z.object({
    name: z.string()
        .min(1, "Name is required")
        .max(MAX_CATEGORY_NAME_LENGTH, `Name must not exceed ${ MAX_CATEGORY_NAME_LENGTH } characters`),
    parentId: z.string().min(1, "Parent category is required")
});

export type CreateCategoryInput = z.infer<typeof createCategoryInputSchema>;

export function createCategory({ data }: { data: CreateCategoryInput }) {
    return api.post(`/api/categories`, data);
}

export function useCreateCategory() {
    const queryClient = useQueryClient();

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getCategoriesQueryOptions().queryKey
            });
        },
        mutationFn: createCategory
    });
}