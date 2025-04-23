import { api } from "@/lib/apiClient.ts";
import { PageResponse, ProductResponse } from "@/types/api.ts";
import { DEFAULT_PAGE_SIZE } from "@/utils/constants.ts";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

export function getProducts(page = 0, search = ""): Promise<AxiosResponse<PageResponse<ProductResponse>>> {
    return api.get(`/api/products/others?pageSize=${ DEFAULT_PAGE_SIZE }&pageNumber=${ page }&search=${ search }`);
}

export function getProductsQueryOptions(
    { page, search }: { page?: number, search?: string } = {}
) {
    return queryOptions({
        queryKey: (page || page === 0 || search)
            ? ["products", { page, search }]
            : ["products"],
        queryFn: () => getProducts(page, search)
    });
}

export type UseGetProductsOptions = {
    page: number;
    search?: string
};

export function useGetProducts({ page, search }: UseGetProductsOptions) {
    return useQuery({
        ...getProductsQueryOptions({ page, search })
    });
}