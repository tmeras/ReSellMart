import { api } from "@/lib/apiClient.ts";
import { CategoryResponse } from "@/types/api.ts";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

export function getParentCategories(): Promise<AxiosResponse<CategoryResponse[]>> {
    return api.get(`/api/categories/parents`);
}

export function getParentCategoriesQueryOptions() {
    return queryOptions({
        queryKey: ["categories", "parents"],
        queryFn: getParentCategories
    });
}

export function useGetParentCategories() {
    return useQuery({
        ...getParentCategoriesQueryOptions()
    });
}