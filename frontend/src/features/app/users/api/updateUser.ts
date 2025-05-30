import { api } from "@/lib/apiClient.ts";
import { UserResponse } from "@/types/api.ts";
import { useMutation } from "@tanstack/react-query";
import { AxiosResponse } from "axios";
import { z } from "zod";

export const updateUserInputSchema = z.object({
    name: z.string().min(1, "Name is required"),
    homeCountry: z.string().min(1, "Home Country is required"),
    isMfaEnabled: z.boolean()
});

export type UpdateUserInput = z.infer<typeof updateUserInputSchema>;

export function updateUser(
    { userId, data }: { userId: string, data: UpdateUserInput }
): Promise<AxiosResponse<UserResponse>> {
    return api.put(`/api/users/${ userId }`, data);
}

export function useUpdateUser() {
    //TODO: Invalidate all users query
    //const queryClient = useQueryClient();

    return useMutation({
        mutationFn: updateUser
    });
}



