import { api } from "@/lib/apiClient.ts";
import { AddressResponse } from "@/types/api.ts";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

export function getAddressesByUser({ userId }: { userId: string }): Promise<AxiosResponse<AddressResponse[]>> {
    return api.get(`/api/users/${ userId }/addresses`);
}

export function getAddressesByUserQueryOptions({ userId }: { userId: string }) {
    return queryOptions({
        queryKey: ["addresses", `user ${ userId }`],
        queryFn: () => getAddressesByUser({ userId })
    });
}

export type UseGetAddressesByUserOptions = {
    userId: string;
};

export function useGetAddressesByUser({ userId }: UseGetAddressesByUserOptions) {
    return useQuery({
        ...getAddressesByUserQueryOptions({ userId })
    });
}
