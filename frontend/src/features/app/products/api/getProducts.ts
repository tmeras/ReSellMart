import { api } from "@/lib/apiClient.ts";
import { PageResponse, ProductResponse } from "@/types/api.ts";
import { DEFAULT_PAGE_SIZE, SORT_DIR, SORT_PRODUCTS_BY } from "@/utils/constants.ts";
import { useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

// @formatter:off

export function getProducts({
    page = 0,
    pageSize = DEFAULT_PAGE_SIZE,
    search = "",
    sortBy = SORT_PRODUCTS_BY,
    sortDirection = SORT_DIR
} : {
    page?: number,
    pageSize?: number,
    search?: string,
    sortBy?: string,
    sortDirection?: string
}) : Promise<AxiosResponse<PageResponse<ProductResponse>>> {
    const params = new URLSearchParams({
        pageNumber: page.toString(),
        pageSize: pageSize.toString(),
        search,
        sortBy,
        sortDirection
    });

    return api.get(`/api/products?${params.toString()}`);
}

export function  getProductsQueryOptions({
    page,
    pageSize,
    search,
    sortBy,
    sortDirection
} : {
    page?: number,
    pageSize?: number,
    search?: string,
    sortBy?: string,
    sortDirection?: string
} = {}) {
    return {
        queryKey: (page || page === 0 || pageSize || pageSize === 0 || search || sortBy || sortDirection)
            ? ["products", { page, pageSize, search, sortBy, sortDirection }]
            : ["products"],
        queryFn: () => getProducts({ page, pageSize, search, sortBy, sortDirection })
    };
}
// @formatter:on

export type UseGetProductsOptions = {
    page?: number;
    pageSize?: number;
    search?: string;
    sortBy?: string;
    sortDirection?: string;
};

export function useGetProducts({ page, pageSize, search, sortBy, sortDirection }: UseGetProductsOptions) {
    return useQuery({
        ...getProductsQueryOptions({ page, pageSize, search, sortBy, sortDirection }),
    });
}