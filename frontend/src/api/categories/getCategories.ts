import { api } from "@/lib/apiClient.ts";
import { CategoryResponse } from "@/types/api.ts";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

export function getCategories(): Promise<AxiosResponse<CategoryResponse[]>> {
    return api.get("/api/categories");
}

export function getCategoriesQueryOptions() {
    return queryOptions({
        queryKey: ["categories"],
        queryFn: getCategories
    });
}

export function useGetCategories() {
    return useQuery({
        ...getCategoriesQueryOptions()
    });
}