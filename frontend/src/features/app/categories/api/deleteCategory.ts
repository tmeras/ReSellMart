import { getCategoriesQueryOptions } from "@/api/categories/getCategories.ts";
import { api } from "@/lib/apiClient.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";

export function deleteCategory({ categoryId }: { categoryId: string }) {
    return api.delete(`/api/categories/${ categoryId }`);
}

export function useDeleteCategory() {
    const queryClient = useQueryClient();

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getCategoriesQueryOptions().queryKey
            });
        },
        mutationFn: deleteCategory
    });
}