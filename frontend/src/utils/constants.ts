//Pagination/Sorting
export const DEFAULT_PAGE_SIZE = 12 as const;
export const SORT_CATEGORIES_BY = "id";
export const SORT_PRODUCTS_BY = "listedAt";
export const SORT_ORDERS_BY = "id";
export const SORT_USERS_BY = "id";
export const SORT_ADDRESSES_BY = "id";
export const SORT_DIR = "asc";

// Files
export const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
export const MAX_IMAGE_COUNT = 5 as const;
export const ACCEPTED_IMAGE_TYPES =
    ["image/jpeg", "image/jpg", "image/png", "image/webp"];
export const ACCEPTED_IMAGE_EXTENSIONS =
    [".jpeg", ".jpg", ".png", ".webp"] as const;

// Products
export const MAX_PRODUCT_NAME_LENGTH = 100 as const;
export const MAX_PRODUCT_DESCRIPTION_LENGTH = 5000 as const;
export const MAX_PRODUCT_PRICE = 10000 as const;
export const MAX_PRODUCT_QUANTITY = 1000 as const;
export const PRODUCT_SORT_OPTIONS = [
    {
        value: "listedAt desc",
        label: "Sort by: Date listed (descending)"
    },
    {
        value: "listedAt asc",
        label: "Sort by: Date listed (ascending)"
    },
    {
        value: "price desc",
        label: "Sort by: Price (descending)"
    },
    {
        value: "price asc",
        label: "Sort by: Price (ascending)"
    }
] as const;

// Product condition
export const PRODUCT_CONDITION = {
    NEW: "New",
    LIKE_NEW: "Like New",
    GOOD: "Good",
    FAIR: "Fair",
    DAMAGED: "Damaged"
} as const;

export type ProductConditionKeys = keyof typeof PRODUCT_CONDITION;

// Address type
export const ADDRESS_TYPE = {
    HOME: "Home",
    WORK: "Work",
    BILLING: "Billing",
    SHIPPING: "Shipping"
} as const;

export type AddressTypeKeys = keyof typeof ADDRESS_TYPE;
