import { api } from "@/lib/apiClient.ts";
import { uploadImageInputSchema, UserResponse } from "@/types/api.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { AxiosResponse } from "axios";
import { z } from "zod";

export const uploadUserImageInputSchema = z.object({
    image: uploadImageInputSchema.nullable()
});

export type UploadUserImageInput = z.infer<typeof uploadUserImageInputSchema>;

export function uploadUserImage(
    { userId, data }: { userId: string, data: UploadUserImageInput }
): Promise<AxiosResponse<UserResponse>> {
    const formData = new FormData();
    if (data.image) formData.append("image", data.image);

    return api.put(`/api/users/${ userId }/image`, formData, {
        headers: {
            "Content-Type": "multipart/form-data"
        }
    });
}

export function useUploadUserImage() {
    //TODO: Invalidate all users query
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: uploadUserImage
    });
}

