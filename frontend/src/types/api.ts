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
    id: string; //TODO: Change to string
    name: string;
    email: string;
    homeCountry: string;
    registeredAt: string;
    mfaEnabled: boolean;
    profileImage?: string; //base64 string
    roles: Role[];
    qrImageUri?: string;
}

export type CategoryResponse = {
    id: string;
    name: string;
    parentId?: string;
}

export type ProductImageResponse = {
    id: string;
    image: string; //base64 string
    name: string;
    type: string;
}

export type ProductResponse = {
    id: string;
    name: string;
    description: string;
    price: number;
    previousPrice: number;
    productCondition: ProductConditionKeys
    availableQuantity: number;
    listedAt: string; // UTC datetime string
    deleted: boolean;
    images: ProductImageResponse[];
    category: CategoryResponse;
    seller: UserResponse;
}

export type CartItemResponse = {
    id: string;
    product: ProductResponse;
    quantity: number;
    addedAt: string;
}

export type WishlistItemResponse = {
    id: string;
    product: ProductResponse;
    addedAt: string;
}

export type AddressResponse = {
    id: string;
    name: string;
    country: string;
    street: string;
    state: string;
    city: string;
    postalCode: string;
    phoneNumber: string;
    main: boolean;
    addressType: AddressTypeKeys;
    userId: string;
}
