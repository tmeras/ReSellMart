import { api } from "@/lib/apiClient.ts";
import { PageResponse, ProductResponse } from "@/types/api.ts";
import { DEFAULT_PAGE_SIZE, SORT_DIR, SORT_PRODUCTS_BY } from "@/utils/constants.ts";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

// @formatter:off

export function getProductsByUser({
    userId,
    page = 0,
    search = "",
    sortBy = SORT_PRODUCTS_BY,
    sortDirection = SORT_DIR
}
: {
    userId: string,
    page?: number,
    search?: string
    sortBy?: string,
    sortDirection?: string
}): Promise<AxiosResponse<PageResponse<ProductResponse>>> {
    const params = new URLSearchParams({
        pageSize: DEFAULT_PAGE_SIZE.toString(),
        pageNumber: page.toString(),
        search,
        sortBy,
        sortDirection
    });

    return api.get(`/api/products/users/${ userId }?${ params.toString() }`);
}

export function getProductsByUserQueryOptions({
    userId,
    page,
    search,
    sortBy,
    sortDirection
} : {
    userId: string;
    page?: number;
    search?: string;
    sortBy?: string;
    sortDirection?: string;
}) {
    return queryOptions({
        queryKey: (page || page === 0 || search || sortBy || sortDirection)
            ? ["products", `user ${ userId }`, { page, search, sortBy, sortDirection }]
            : ["products", `user ${ userId }`],
        queryFn: () => getProductsByUser({userId, page, search, sortBy, sortDirection})
    });
}
// @formatter:on

export type UseGetProductsByUserOptions = {
    userId: string;
    page: number;
    search: string;
    sortBy: string;
    sortDirection: string;
};

export function useGetProductsByUser({ userId, page, search, sortBy, sortDirection }: UseGetProductsByUserOptions) {
    return useQuery({
        ...getProductsByUserQueryOptions({ userId, page, search, sortBy, sortDirection })
    });
}
