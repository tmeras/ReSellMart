export function base64ToDataUri(base64: string | undefined, mimeType = "image/png") {
    if (!base64) return null;

    return `data:${ mimeType };base64,${ base64 }`;
}

export function base64ToFile(base64: string, fileName: string, mimeType = "image/png") {
    const byteString = atob(base64); // Decode base64
    const byteArray = new Uint8Array(byteString.length);

    for (let i = 0; i < byteString.length; i++) {
        byteArray[i] = byteString.charCodeAt(i);
    }

    return new File([byteArray], fileName, { type: mimeType });
}

export async function imageUrlToFile(url: string, filename: string, mimeType: string) {
    const response = await fetch(url);
    const blob = await response.blob();
    return new File([blob], filename, { type: mimeType });
}