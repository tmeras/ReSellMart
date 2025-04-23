export function base64ToDataUri(base64: string | undefined, imageType = "image/png") {
    if (!base64) return null;

    return `data:${ imageType };base64,${ base64 }`;
}

export function base64ToFile(base64: string, fileName: string, mimeType: string) {
    const byteString = atob(base64); // Decode base64
    const byteArray = new Uint8Array(byteString.length);

    for (let i = 0; i < byteString.length; i++) {
        byteArray[i] = byteString.charCodeAt(i);
    }

    return new File([byteArray], fileName, { type: mimeType });
}