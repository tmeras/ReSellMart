import { getAddressesByUserQueryOptions } from "@/features/app/addresses/api/getAddressesByUser.ts";
import { api } from "@/lib/apiClient.ts";
import { AddressResponse } from "@/types/api.ts";
import { ADDRESS_TYPE, AddressTypeKeys } from "@/utils/constants.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { AxiosResponse } from "axios";
import { z } from "zod";

export const createAddressInputSchema = z.object({
    name: z.string().min(1, "Name is required"),
    country: z.string().min(1, "Country is required"),
    street: z.string().min(1, "Street is required"),
    state: z.string().min(1, "State is required"),
    city: z.string().min(1, "City is required"),
    postalCode: z.string().min(1, "Postal code is required"),
    phoneNumber: z.string().optional()
        .refine((phone) => !phone || /^\+[0-9 ()-]{7,25}$/.test(phone),
            "Phone number must start with '+' and contain only digits, spaces, " +
            "parentheses, or dashes, and be between 7 and 25 characters."
        ),
    isMain: z.boolean().default(false).optional(),
    addressType: z.enum(Object.keys(ADDRESS_TYPE) as [AddressTypeKeys])
});

export type CreateAddressInput = z.infer<typeof createAddressInputSchema>;

export function createAddress({ data }: { data: CreateAddressInput }): Promise<AxiosResponse<AddressResponse>> {
    return api.post("/api/addresses", data);
}

export type UseCreateAddressOptions = {
    userId: string;
};

export function useCreateAddress({ userId }: UseCreateAddressOptions) {
    const queryClient = useQueryClient();

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getAddressesByUserQueryOptions({ userId }).queryKey,
                exact: true
            });
        },
        mutationFn: createAddress
    });
}
