import { api } from "@/lib/apiClient.ts";
import { PageResponse, ProductResponse } from "@/types/api.ts";
import { DEFAULT_PAGE_SIZE, SORT_DIR, SORT_PRODUCTS_BY } from "@/utils/constants.ts";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

// @formatter:off
export function getOtherUsersProducts({
    page = 0,
    search = "",
    sortBy = SORT_PRODUCTS_BY,
    sortDirection = SORT_DIR
}
: {
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

    return api.get(`/api/products/others?${ params.toString() }`);
}

export function getOtherUsersProductsQueryOptions({
    page,
    search,
    sortBy,
    sortDirection
}: {
    page?: number;
    search?: string;
    sortBy?: string;
    sortDirection?: string;
} = {}) {
    return queryOptions({
        queryKey: (page || page === 0 || search || sortBy || sortDirection)
            ? ["products", "others", { page, search, sortBy, sortDirection }]
            : ["products", "others"],
        queryFn: () => getOtherUsersProducts({page, search, sortBy, sortDirection})
    });
}
// @formatter:on

export type UseGetOtherUsersProductsOptions = {
    page?: number;
    search?: string;
    sortBy?: string;
    sortDirection?: string;
};

export function useGetOtherUsersProducts({ page, search, sortBy, sortDirection }: UseGetOtherUsersProductsOptions) {
    return useQuery({
        ...getOtherUsersProductsQueryOptions({ page, search, sortBy, sortDirection })
    });
}