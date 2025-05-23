import { getAddressesByUserQueryOptions } from "@/api/addresses/getAddressesByUser.ts";
import { api } from "@/lib/apiClient.ts";
import { AddressResponse } from "@/types/api.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

export function makeAddressMain({ addressId }: { addressId: string }): Promise<AxiosResponse<AddressResponse>> {
    return api.patch(`/api/addresses/${ addressId }/main`);
}

export type UseMakeAddressMainOptions = {
    userId: string;
};

export function useMakeAddressMain({ userId }: UseMakeAddressMainOptions) {
    const queryClient = useQueryClient();

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getAddressesByUserQueryOptions({ userId }).queryKey,
                exact: true
            });
        },
        mutationFn: makeAddressMain
    });
}