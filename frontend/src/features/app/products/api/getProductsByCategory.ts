import { api } from "@/lib/apiClient.ts";
import { PageResponse, ProductResponse } from "@/types/api.ts";
import { DEFAULT_PAGE_SIZE } from "@/utils/constants.ts";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

export function getProductsByCategory(categoryId: string, page = 0, search = ""): Promise<AxiosResponse<PageResponse<ProductResponse>>> {
    return api.get(`/api/products/categories/${ categoryId }?pageSize=${ DEFAULT_PAGE_SIZE }&pageNumber=${ page }&search=${ search }`);
}

export function getProductsByCategoryQueryOptions(
    { categoryId, page, search }: { categoryId: string, page?: number, search?: string }
) {
    return queryOptions({
        queryKey: (page || page === 0 || search)
            ? ["products", `category ${ categoryId }`, { page, search }]
            : ["products", `category ${ categoryId }`],
        queryFn: () => getProductsByCategory(categoryId, page, search)
    });
}

export type UseGetProductsByCategoryOptions = {
    categoryId: string;
    page: number;
    search?: string;
};

export function useGetProductsByCategory({ categoryId, page, search }: UseGetProductsByCategoryOptions) {
    return useQuery({
        ...getProductsByCategoryQueryOptions({ categoryId, page, search })
    });
}

