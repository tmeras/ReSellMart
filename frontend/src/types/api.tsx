export type AuthenticationResponse = {
    accessToken: string;
    mfaEnabled: boolean;
    qrImageUri?: string;
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
    profileImage: Uint8Array;
    roles: Role[];
    qrImageUri?: string;
}

