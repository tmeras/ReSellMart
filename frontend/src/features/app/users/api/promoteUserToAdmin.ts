import { getUsersQueryOptions } from "@/features/app/users/api/getUsers.ts";
import { api } from "@/lib/apiClient.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";

export function promoteUserToAdmin({userId} : {userId: string}) {
    return api.post(`/api/users/${userId}/promote`)
}

export function usePromoteUserToAdmin() {
    const queryClient = useQueryClient();

    return useMutation({
        onSuccess: async() => {
            await queryClient.invalidateQueries({
                queryKey: getUsersQueryOptions().queryKey
            });
        },
        mutationFn: promoteUserToAdmin
    });
}