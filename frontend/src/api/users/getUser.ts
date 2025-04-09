import { api } from "@/lib/apiClient.ts";
import { UserResponse } from "@/types/api.tsx";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

export function getUser(userId: string): Promise<AxiosResponse<UserResponse>> {
    return api.get(`/api/users/${ userId }`);
}

export function getUserQueryOptions({ userId }: { userId: string }) {
    return queryOptions({
        queryKey: ["users", userId],
        queryFn: () => getUser(userId)
    });
}

export type UseGetUserOptions = {
    userId: string;
};

export function useGetUser({ userId }: UseGetUserOptions) {
    return useQuery({
        ...getUserQueryOptions({ userId })
    });
}