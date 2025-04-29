import { api } from "@/lib/apiClient.ts";
import { ProductResponse } from "@/types/api.ts";
import { queryOptions, useQuery } from "@tanstack/react-query";
import { AxiosResponse } from "axios";

export function getProduct({ productId }: { productId: string }): Promise<AxiosResponse<ProductResponse>> {
    return api.get(`/api/products/${ productId }`);
}

export function getProductQueryOptions({ productId }: { productId: string }) {
    return queryOptions({
        queryKey: ["products", productId],
        queryFn: () => getProduct({ productId })
    });
}

export type UseGetProductOptions = {
    productId: string;
};

export function useGetProduct({ productId }: UseGetProductOptions) {
    return useQuery({
        ...getProductQueryOptions({ productId })
    });
}