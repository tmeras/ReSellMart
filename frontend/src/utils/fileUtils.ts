import { ProductImageResponse } from "@/types/api.tsx";

export function bytesToBase64(bytes: Uint8Array | undefined) {
    if (!bytes) return null;

    return "data:image/png;base64," + bytes;
}

export function findDisplayedImage(images: ProductImageResponse[]) {
    return images.find(image => image.displayed);
}