import { api } from "@/lib/apiClient";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

export function getCartTotal({ userId }: { userId: string }): Promise<AxiosResponse<number>> {
    return api.get(`/api/users/${ userId }/cart/total`);
}

export function getCartTotalQueryOptions({ userId }: { userId: string }) {
    return queryOptions({
        queryKey: ["users", userId, "cart", "total"],
        queryFn: () => getCartTotal({ userId })
    });
}

export type UseGetCartTotalOptions = {
    userId: string;
};

export function useGetCartTotal({ userId }: UseGetCartTotalOptions) {
    return useQuery({
        ...getCartTotalQueryOptions({ userId })
    });
}