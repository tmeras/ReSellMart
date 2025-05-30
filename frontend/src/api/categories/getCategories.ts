import { api } from "@/lib/apiClient.ts";
import { CategoryResponse } from "@/types/api.ts";
import { SORT_CATEGORIES_BY, SORT_DIR } from "@/utils/constants.ts";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

// @formatter:off
export function getCategories({
    search = "",
    sortBy = SORT_CATEGORIES_BY,
    sortDirection = SORT_DIR
} : {
    search?: string,
    sortBy?: string,
    sortDirection?: string
}): Promise<AxiosResponse<CategoryResponse[]>> {
    const params = new URLSearchParams({
        search,
        sortBy,
        sortDirection
    });

    return api.get(`/api/categories?${params.toString()}`);
}

export function getCategoriesQueryOptions({
    search,
    sortBy,
    sortDirection
} : {
    search?: string,
    sortBy?: string,
    sortDirection?: string
} = {}) {
    return queryOptions({
        queryKey: (search || sortBy || sortDirection)
            ? ["categories", { search, sortBy, sortDirection }]
            : ["categories"],
        queryFn: () => getCategories({search, sortBy, sortDirection})
    });
}
// @formatter:on

export type UseGetCategoriesOptions = {
    search?: string;
    sortBy?: string;
    sortDirection?: string;
};

export function useGetCategories({ search, sortBy, sortDirection }: UseGetCategoriesOptions) {
    return useQuery({
        ...getCategoriesQueryOptions({ search, sortBy, sortDirection })
    });
}