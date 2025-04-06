import { ProductImageResponse } from "@/types/api.tsx";

export function byteToBase64(bytes: Uint8Array) {
    return "data:image/png;base64," + bytes;
}

export function findDisplayedImage(images: ProductImageResponse[]) {
    return images.find(image => image.displayed);
}