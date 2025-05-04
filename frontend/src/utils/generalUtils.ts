import { CartItemResponse } from "@/types/api.ts";

export function shuffleArray<T>(array: T[]): T[] {
    for (let i = array.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [array[i], array[j]] = [array[j], array[i]]; // swap elements
    }
    return array;
}

// Check if the cart is valid; i.e. that all products must be available
// and requested quantities must not exceed available stock
export function isCartValid(cartItems: CartItemResponse[]) {
    return cartItems.every((item) =>
        !item.product.isDeleted && (item.quantity <= item.product.availableQuantity)
    );
}