import {
    UpdateAddressInput,
    updateAddressInputSchema,
    useUpdateAddress
} from "@/features/app/addresses/api/updateAddress.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { AddressResponse } from "@/types/api.ts";
import { ADDRESS_TYPE, AddressTypeKeys } from "@/utils/constants.ts";
import { Button, Flex, Modal, Select, TextInput } from "@mantine/core";
import { useForm, zodResolver } from "@mantine/form";
import { notifications } from "@mantine/notifications";
import { IconEdit, IconX } from "@tabler/icons-react";
import { Country } from "country-state-city";
import { useEffect, useMemo, useState } from "react";

export type UpdateAddressButtonProps = {
    address: AddressResponse;
};

export function UpdateAddressButton({ address }: UpdateAddressButtonProps) {
    const { user } = useAuth();
    const [modalOpened, setModalOpened] = useState(false);

    const countryOptions = useMemo(() => {
        const countries = Country.getAllCountries();
        return countries.map((country) => country.name);
    }, []);

    const updateAddressMutation = useUpdateAddress({ userId: user!.id.toString() });

    const form = useForm<UpdateAddressInput>({
        mode: "uncontrolled",
        initialValues: {
            name: user!.name,
            country: "",
            street: "",
            state: "",
            city: "",
            postalCode: "",
            phoneNumber: "",
            addressType: "HOME"
        },
        validate: zodResolver(updateAddressInputSchema),
        // Disable form until initial data is fetched from API
        enhanceGetInputProps: (payload) => {
            if (!payload.form.initialized) {
                return { disabled: true };
            }

            return {};
        }
    });

    // Initialise form using address data received from API
    useEffect(() => {
        if (!form.initialized) form.initialize({ ...address });
    }, [address]);

    async function handleSubmit(values: typeof form.values) {
        try {
            // Don't send empty phone string
            if (values.phoneNumber === "")
                values.phoneNumber = undefined;

            await updateAddressMutation.mutateAsync({
                addressId: address.id.toString(),
                data: values
            });

            setModalOpened(false);
            notifications.show({
                title: "Address updated successfully", message: "",
                color: "teal", withBorder: true
            });
        } catch (error) {
            console.log("Error updating address", error);
            notifications.show({
                title: "Something went wrong", message: "Please try updating the address again",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    const typeOptions = Object.keys(ADDRESS_TYPE).map((type) => {
        return {
            label: ADDRESS_TYPE[type as AddressTypeKeys],
            value: type
        };
    });

    return (
        <>
            <Modal
                opened={ modalOpened } title="Update Address" centered
                onClose={ () => {
                    setModalOpened(false);
                } }
                size="sm"
            >
                <form onSubmit={ form.onSubmit(handleSubmit) }>
                    <TextInput
                        label="Name on address" required withAsterisk={ false }
                        key={ form.key("name") }
                        { ...form.getInputProps("name") }
                    />

                    <Select
                        mt="sm"
                        label="Country" required withAsterisk={ false }
                        data={ countryOptions } searchable
                        nothingFoundMessage="Nothing found..."
                        key={ form.key("country") }
                        { ...form.getInputProps("country") }
                    />

                    <TextInput
                        mt="sm"
                        label="Street" required withAsterisk={ false }
                        key={ form.key("street") }
                        { ...form.getInputProps("street") }
                    />

                    <TextInput
                        mt="sm"
                        label="State" required withAsterisk={ false }
                        key={ form.key("state") }
                        { ...form.getInputProps("state") }
                    />

                    <TextInput
                        mt="sm"
                        label="City" required withAsterisk={ false }
                        key={ form.key("city") }
                        { ...form.getInputProps("city") }
                    />

                    <Flex gap="xs">
                        <TextInput
                            mt="sm"
                            label="Postal code" required withAsterisk={ false }
                            key={ form.key("postalCode") }
                            { ...form.getInputProps("postalCode") }
                        />

                        <Select
                            mt="sm"
                            label="Address type" required withAsterisk={ false }
                            data={ typeOptions }
                            key={ form.key("addressType") }
                            { ...form.getInputProps("addressType") }
                        />
                    </Flex>

                    <TextInput
                        mt="sm"
                        label="Phone number(optional)"
                        key={ form.key("phoneNumber") }
                        { ...form.getInputProps("phoneNumber") }
                    />

                    <Button type="submit" mt="xl" fullWidth loading={ form.submitting }>
                        Update address
                    </Button>
                </form>
            </Modal>

            <Button
                variant="light" size="compact-sm"
                leftSection={ <IconEdit size={ 16 }/> }
                onClick={ () => setModalOpened(true) }
            >
                Edit
            </Button>
        </>
    );
}