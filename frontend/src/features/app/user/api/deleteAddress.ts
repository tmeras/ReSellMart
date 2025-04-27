import { getAddressesByUserQueryOptions } from "@/features/app/user/api/getAddressesByUser.ts";
import { api } from "@/lib/apiClient.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";

export function deleteAddress({ addressId }: { addressId: string }) {
    return api.delete(`/api/addresses/${ addressId }`);
}

export type UseDeleteAddressOptions = {
    userId: string;
}

export function useDeleteAddress({ userId }: UseDeleteAddressOptions) {
    const queryClient = useQueryClient();

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getAddressesByUserQueryOptions({ userId }).queryKey,
                exact: true
            });
        },
        mutationFn: deleteAddress
    });
}