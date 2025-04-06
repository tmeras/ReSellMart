export function byteToBase64(bytes: Uint8Array) {
    return "data:image/png;base64," + bytes;
}