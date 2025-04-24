import { api } from "@/lib/apiClient.ts";
import { PageResponse, ProductResponse } from "@/types/api.ts";
import { DEFAULT_PAGE_SIZE, SORT_DIR, SORT_PRODUCTS_BY } from "@/utils/constants.ts";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

export function getProductsByCategory(
    categoryId: string,
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

    return api.get(`/api/products/categories/${ categoryId }?${ params.toString() }`);
}

export function getProductsByCategoryQueryOptions({
                                                      categoryId,
                                                      page,
                                                      search,
                                                      sortBy,
                                                      sortDirection
                                                  }: {
    categoryId: string;
    page?: number;
    search?: string;
    sortBy?: string;
    sortDirection?: string;
}) {
    return queryOptions({
        queryKey: (page || page === 0 || search || sortBy || sortDirection)
            ? ["products", `category ${ categoryId }`, { page, search, sortBy, sortDirection }]
            : ["products", `category ${ categoryId }`],
        queryFn: () => getProductsByCategory(categoryId, page, search, sortBy, sortDirection)
    });
}

export type UseGetProductsByCategoryOptions = {
    categoryId: string;
    page: number;
    search: string;
    sortBy: string;
    sortDirection: string;
};

export function useGetProductsByCategory({
                                             categoryId,
                                             page,
                                             search,
                                             sortBy,
                                             sortDirection
                                         }: UseGetProductsByCategoryOptions) {
    return useQuery({
        ...getProductsByCategoryQueryOptions({ categoryId, page, search, sortBy, sortDirection })
    });
}

