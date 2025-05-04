import {
    ACCEPTED_IMAGE_EXTENSIONS,
    ACCEPTED_IMAGE_TYPES,
    AddressTypeKeys,
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

export type PageResponse<T> = {
    content: T[];
    pageNumber: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    isFirst: boolean;
    isLast: boolean;
}

export type AuthenticationResponse = {
    accessToken: string;
    isMfaEnabled: boolean;
}

export type RegistrationResponse = {
    isMfaEnabled: boolean;
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
    isMfaEnabled: boolean;
    profileImage?: string; //base64 string
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
    image: string; //base64 string
    name: string;
    type: string;
}

export type ProductResponse = {
    id: number;
    name: string;
    description: string;
    price: number;
    previousPrice: number;
    productCondition: ProductConditionKeys
    availableQuantity: number;
    listedAt: string; // UTC datetime string
    isDeleted: boolean;
    images: ProductImageResponse[];
    category: CategoryResponse;
    seller: UserResponse;
}

export type CartItemResponse = {
    id: number;
    product: ProductResponse;
    quantity: number;
    price: number;
    addedAt: string;
}

export type WishlistItemResponse = {
    id: number;
    product: ProductResponse;
    addedAt: string;
}

export type AddressResponse = {
    id: number;
    name: string;
    country: string;
    street: string;
    state: string;
    city: string;
    postalCode: string;
    phoneNumber: string;
    isMain: boolean;
    addressType: AddressTypeKeys;
    userId: number;
}
