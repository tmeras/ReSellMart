import { api } from "@/lib/apiClient.ts";
import { AdminUserResponse, PageResponse } from "@/types/api.ts";
import { DEFAULT_PAGE_SIZE, SORT_DIR, SORT_USERS_BY } from "@/utils/constants.ts";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

// @formatter:off

export function getUsers({
    page = 0,
    pageSize = DEFAULT_PAGE_SIZE,
    search = "",
    sortBy = SORT_USERS_BY,
    sortDirection = SORT_DIR
} : {
    page?: number,
    pageSize?: number,
    search?: string,
    sortBy?: string,
    sortDirection?: string
}) : Promise<AxiosResponse<PageResponse<AdminUserResponse>>> {
    const params = new URLSearchParams({
        pageNumber: page.toString(),
        pageSize: pageSize.toString(),
        search,
        sortBy,
        sortDirection
    });

    return api.get(`/api/users?${params.toString()}`)
}

export function getUsersQueryOptions({
     page,
     pageSize,
     search,
     sortBy,
     sortDirection
} : {
    page?: number,
    pageSize?: number,
    search?: string,
    sortBy?: string,
    sortDirection?: string
} = {}) {
    return queryOptions({
        queryKey: (page || page === 0 || pageSize || pageSize === 0 ||search || sortBy || sortDirection)
            ? ["users", { page, pageSize, search, sortBy, sortDirection }]
            : ["users"],
        queryFn: () => getUsers({ page, pageSize, search, sortBy, sortDirection })
    });
}
// @formatter:on

export type UseGetUsersOptions = {
    page?: number;
    pageSize?: number;
    search?: string;
    sortBy?: string;
    sortDirection?: string;
};

export function useGetUsers({ page, pageSize, search, sortBy, sortDirection }: UseGetUsersOptions) {
    return useQuery({
        ...getUsersQueryOptions({ page, pageSize, search, sortBy, sortDirection })
    });
}