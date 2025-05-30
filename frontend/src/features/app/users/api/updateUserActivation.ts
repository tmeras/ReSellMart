import { getProductsQueryOptions } from "@/features/app/products/api/getProducts.ts";
import { getUsersQueryOptions } from "@/features/app/users/api/getUsers.ts";
import { api } from "@/lib/apiClient.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";

export function updateUserActivation(
    { userId, data }: { userId: string, data: { isEnabled: boolean } }
) {
    return api.patch(`/api/users/${ userId }/activation`, data);
}

export function useUpdateUserActivation() {
    const queryClient = useQueryClient();

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getUsersQueryOptions().queryKey
            });

            // Products associated with the user will also be soft-deleted
            // so invalidate product queries as well
            await queryClient.invalidateQueries({
                queryKey: getProductsQueryOptions().queryKey
            });
        },
        mutationFn: updateUserActivation
    });
}