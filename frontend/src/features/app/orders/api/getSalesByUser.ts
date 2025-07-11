import { api } from "@/lib/apiClient.ts";
import { OrderResponse, PageResponse } from "@/types/api.ts";
import { SORT_DIR, SORT_ORDERS_BY } from "@/utils/constants.ts";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

// @formatter:off
export function getSalesByUser({
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

    return api.get(`/api/users/${userId}/sales?${ params.toString() }`);
}

export function getSalesByUserQueryOptions({
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
            ? ["orders", `seller ${ userId }`, { page, sortBy, sortDirection }]
            : ["orders", `seller ${ userId }`],
        queryFn: () => getSalesByUser({userId, page, sortBy, sortDirection})
    });
}
// @formatter:on

export type UseGetSalesByUserOptions = {
    userId: string;
    page: number;
    sortBy: string;
    sortDirection: string;
};

export function useGetSalesByUser({ userId, page, sortBy, sortDirection }: UseGetSalesByUserOptions) {
    return useQuery({
        ...getSalesByUserQueryOptions({ userId, page, sortBy, sortDirection })
    });
}