import { getAddressesByUserQueryOptions } from "@/features/app/addresses/api/getAddressesByUser.ts";
import { api } from "@/lib/apiClient.ts";
import { AddressResponse } from "@/types/api.ts";
import { ADDRESS_TYPE, AddressTypeKeys } from "@/utils/constants.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { AxiosResponse } from "axios";
import { z } from "zod";

export const updateAddressInputSchema = z.object({
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
    addressType: z.enum(Object.keys(ADDRESS_TYPE) as [AddressTypeKeys])
});

export type UpdateAddressInput = z.infer<typeof updateAddressInputSchema>;

export function updateAddress(
    { addressId, data }: { addressId: string, data: UpdateAddressInput }
): Promise<AxiosResponse<AddressResponse>> {
    return api.put(`/api/addresses/${ addressId }`, data);
}

export type UseUpdateAddressOptions = {
    userId: string;
};

export function useUpdateAddress({ userId }: UseUpdateAddressOptions) {
    const queryClient = useQueryClient();

    return useMutation({
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: getAddressesByUserQueryOptions({ userId }).queryKey,
                exact: true
            });
        },
        mutationFn: updateAddress
    });
}