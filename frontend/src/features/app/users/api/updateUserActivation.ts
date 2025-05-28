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
        },
        mutationFn: updateUserActivation
    });
}