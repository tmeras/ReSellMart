import { api } from "@/lib/apiClient.ts";
import { PageResponse, ProductResponse } from "@/types/api.ts";
import { DEFAULT_PAGE_SIZE, SORT_DIR, SORT_PRODUCTS_BY } from "@/utils/constants.ts";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

export function getProducts(
    page = 0,
    search = "",
    sortBy = SORT_PRODUCTS_BY,
    sortDirection = SORT_DIR
): Promise<AxiosResponse<PageResponse<ProductResponse>>> {
    const params = new URLSearchParams({
        pageSize: DEFAULT_PAGE_SIZE.toString(),
        pageNumber: page.toString(),
        search,
        sortBy,
        sortDirection
    });

    return api.get(`/api/products/others?${ params.toString() }`);
}

export function getProductsQueryOptions({
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
            ? ["products", { page, search, sortBy, sortDirection }]
            : ["products"],
        queryFn: () => getProducts(page, search, sortBy, sortDirection)
    });
}

export type UseGetProductsOptions = {
    page: number;
    search: string;
    sortBy: string;
    sortDirection: string;
};

export function useGetProducts({ page, search, sortBy, sortDirection }: UseGetProductsOptions) {
    return useQuery({
        ...getProductsQueryOptions({ page, search, sortBy, sortDirection })
    });
}