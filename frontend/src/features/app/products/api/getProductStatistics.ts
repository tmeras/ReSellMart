import { api } from "@/lib/apiClient.ts";
import { ProductStatsResponse } from "@/types/api.ts";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

export function getProductStatistics() : Promise<AxiosResponse<ProductStatsResponse>> {
    return api.get("/api/products/statistics");
}

export function getProductStatisticsQueryOptions() {
    return queryOptions({
        queryKey: ["products", "statistics"],
        queryFn: getProductStatistics
    });
}

export function useGetProductStatistics() {
    return useQuery({
        ...getProductStatisticsQueryOptions()
    });
}