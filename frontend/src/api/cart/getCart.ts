import { api } from "@/lib/apiClient.ts";
import { CartItemResponse } from "@/types/api.tsx";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

export function getCart(userId: number): Promise<AxiosResponse<CartItemResponse[]>> {
    return api.get(`/api/users/${ userId }/cart/products`);
}

export function getCartQueryOptions({ userId }: { userId: number }) {
    return queryOptions({
        queryKey: ["users", { userId }, "cart"],
        queryFn: () => getCart(userId)
    });
}

export type UseGetCartOptions = {
    userId: number;
};

export function useGetCart({ userId }: UseGetCartOptions) {
    return useQuery({
        ...getCartQueryOptions({ userId })
    });
}