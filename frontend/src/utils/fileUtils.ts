export function bytesToBase64(bytes: string | undefined, imageType: string) {
    if (!bytes) return null;

    return `data:${ imageType };base64,${ bytes }`;
}

export function base64ToFile(base64: string, fileName: string, mimeType: string) {
    const byteString = atob(base64); // Decode base64
    const byteArray = new Uint8Array(byteString.length);

    for (let i = 0; i < byteString.length; i++) {
        byteArray[i] = byteString.charCodeAt(i);
    }

    return new File([byteArray], fileName, { type: mimeType });
}