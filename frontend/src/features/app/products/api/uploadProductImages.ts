import { getOtherUsersProductsQueryOptions } from "@/features/app/products/api/getOtherUsersProducts.ts";
import { api } from "@/lib/apiClient.ts";
import { ProductResponse, uploadImageInputSchema } from "@/types/api.ts";
import { MAX_IMAGE_COUNT } from "@/utils/constants.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { AxiosResponse } from "axios";
import { z } from "zod";

export const uploadProductImagesInputSchema = z.object({
    images: z.array(uploadImageInputSchema)
        .min(1, "You must upload at least 1 image")
        .max(MAX_IMAGE_COUNT, `You can upload a maximum of ${ MAX_IMAGE_COUNT } images`)
});

export type UploadProductImagesInput = z.infer<typeof uploadProductImagesInputSchema>;

export function uploadProductImages(
    { productId, data }: { productId: string, data: UploadProductImagesInput }
): Promise<AxiosResponse<ProductResponse>> {
    const formData = new FormData();
    data.images.forEach((image) => {
        formData.append("images", image);
    });

    return api.put(`/api/products/${ productId }/images`, formData, {
        headers: {
            "Content-Type": "multipart/form-data"
        }
    });
}

export function useUploadProductImages() {
    const queryClient = useQueryClient();

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getOtherUsersProductsQueryOptions().queryKey
            });
        },
        mutationFn: uploadProductImages
    });
}



