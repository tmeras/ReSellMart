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
    parentId: number;
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
    productCondition: "NEW" | "LIKE_NEW" | "GOOD" | "FAIR" | "DAMAGED";
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
