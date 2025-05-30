import { api } from "@/lib/apiClient.ts";
import { OrderStatsResponse } from "@/types/api.ts";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

export function getOrderStatistics() : Promise<AxiosResponse<OrderStatsResponse>> {
    return api.get("/api/orders/statistics");
}

export function getOrderStatisticsQueryOptions() {
    return queryOptions({
        queryKey: ["orders", "statistics"],
        queryFn: getOrderStatistics
    });
}

export function useGetOrderStatistics() {
    return useQuery({
        ...getOrderStatisticsQueryOptions()
    });
}
