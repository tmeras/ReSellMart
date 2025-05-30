import { api } from "@/lib/apiClient.ts";
import { UserStatsResponse } from "@/types/api.ts";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

export function getUserStatistics() : Promise<AxiosResponse<UserStatsResponse>> {
    return api.get("/api/users/statistics");
}

export function getUserStatisticsQueryOptions() {
    return queryOptions({
        queryKey: ["users", "statistics"],
        queryFn: getUserStatistics
    });
}

export function useGetUserStatistics() {
    return useQuery({
        ...getUserStatisticsQueryOptions()
    });
}