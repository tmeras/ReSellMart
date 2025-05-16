import { api } from "@/lib/apiClient";
import { OrderResponse, PageResponse } from "@/types/api.ts";
import { SORT_DIR, SORT_ORDERS_BY } from "@/utils/constants.ts";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

// @formatter:off
export function getPurchasesByUser({
    userId,
    page = 0,
    sortBy = SORT_ORDERS_BY,
    sortDirection = SORT_DIR
} : {
    userId: string,
    page?: number,
    sortBy?: string,
    sortDirection?: string
}): Promise<AxiosResponse<PageResponse<OrderResponse>>> {
    const params = new URLSearchParams({
        pageSize: "5",
        pageNumber: page.toString(),
        sortBy,
        sortDirection
    });

    return api.get(`/api/users/${userId}/purchases?${ params.toString() }`);
}

export function getPurchasesByUserQueryOptions({
   userId,
   page,
   sortBy,
   sortDirection
} : {
    userId: string,
    page?: number,
    sortBy?: string,
    sortDirection?: string
}) {
    return queryOptions({
        queryKey: (page || page === 0 || sortBy || sortDirection)
            ? ["orders", `buyer ${ userId }`, { page, sortBy, sortDirection }]
            : ["orders", `buyer ${ userId }`],
        queryFn: () => getPurchasesByUser({userId, page, sortBy, sortDirection})
    });
}
// @formatter:on

export type UseGetPurchasesByUserOptions = {
    userId: string;
    page: number;
    sortBy: string;
    sortDirection: string;
}

export function useGetPurchasesByUser({ userId, page, sortBy, sortDirection }: UseGetPurchasesByUserOptions) {
    return useQuery(getPurchasesByUserQueryOptions({ userId, page, sortBy, sortDirection }));
}