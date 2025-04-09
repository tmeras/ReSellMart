import { api } from "@/lib/apiClient.ts";
import { WishlistItemResponse } from "@/types/api.tsx";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

export function getWishlist(userId: number): Promise<AxiosResponse<WishlistItemResponse[]>> {
    return api.get(`/api/users/${ userId }/wishlist/products`);
}

export function getWishlistQueryOptions({ userId }: { userId: number }) {
    return queryOptions({
        queryKey: ["user", { userId }, "wishlist"],
        queryFn: () => getWishlist(userId)
    });
}

export type UseGetWishlistOptions = {
    userId: number;
};

export function useGetWishlist({ userId }: UseGetWishlistOptions) {
    return useQuery({
        ...getWishlistQueryOptions({ userId })
    });
}