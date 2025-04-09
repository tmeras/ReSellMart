import { api } from "@/lib/apiClient.ts";
import { PageResponse, ProductResponse } from "@/types/api.tsx";
import { DEFAULT_PAGE_SIZE } from "@/utils/constants.ts";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

export function getProductsByUser(userId: string, page = 0, search = ""): Promise<AxiosResponse<PageResponse<ProductResponse>>> {
    return api.get(`/api/products/users/${ userId }?pageSize=${ DEFAULT_PAGE_SIZE }&pageNumber=${ page }&search=${ search }`);
}

export function getProductsByUserQueryOptions(
    { userId, page = 0, search }: { userId: string, page?: number, search?: string }
) {
    return queryOptions({
        queryKey: (page || page === 0) ?
            (search ? ["products", `user ${ userId }`, { page }, { search }]
                    : ["products", `user ${ userId }`, { page }]
            )
            : search ? ["products", `user ${ userId }`, { search }]
                : ["products", `user ${ userId }`],
        queryFn: () => getProductsByUser(userId, page, search)
    });
}

export type UseGetProductsByUserOptions = {
    userId: string;
    page: number;
    search?: string;
};

export function useGetProductsByUser({ userId, page, search }: UseGetProductsByUserOptions) {
    return useQuery({
        ...getProductsByUserQueryOptions({ userId, page, search })
    });
}
