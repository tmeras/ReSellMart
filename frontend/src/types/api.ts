import {
    ACCEPTED_IMAGE_EXTENSIONS,
    ACCEPTED_IMAGE_TYPES,
    MAX_FILE_SIZE,
    ProductConditionKeys
} from "@/utils/constants.ts";
import { z } from "zod";

export const uploadImageInputSchema = z.instanceof(File)
    .refine(file => file.size <= MAX_FILE_SIZE, {
        message: `File size must be no greater than ${ MAX_FILE_SIZE / (1024 * 1024) }MB`
    })
    .refine(file => ACCEPTED_IMAGE_TYPES.includes(file.type), {
        message: `File type must be one of ${ ACCEPTED_IMAGE_EXTENSIONS.join(", ") }`
    });

export type UploadImageInput = z.infer<typeof uploadImageInputSchema>;

export type PageResponse<T> = {
    content: T[];
    pageNumber: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
}

export type AuthenticationResponse = {
    accessToken: string;
    mfaEnabled: boolean;
}

export type RegistrationResponse = {
    mfaEnabled: boolean;
    qrImageUri?: string;
}

export type Role = {
    id: number;
    name: string;
}

export type UserResponse = {
    id: number;
    name: string;
    email: string;
    homeCountry: string;
    registeredAt: string;
    mfaEnabled: boolean;
    profileImage?: Uint8Array;
    roles: Role[];
    qrImageUri?: string;
}

export type CategoryResponse = {
    id: number;
    name: string;
    parentId?: number;
}

export type ProductImageResponse = {
    id: number;
    image: Uint8Array;
    displayed: boolean;
}

export type ProductResponse = {
    id: number;
    name: string;
    description: string;
    price: number;
    previousPrice: number;
    productCondition: ProductConditionKeys
    availableQuantity: number;
    deleted: boolean;
    images: ProductImageResponse[];
    category: CategoryResponse;
    seller: UserResponse;
}

export type CartItemResponse = {
    id: number;
    product: ProductResponse;
    quantity: number;
    addedAt: string;
}

export type WishlistItemResponse = {
    id: number;
    product: ProductResponse;
    addedAt: string;
}
