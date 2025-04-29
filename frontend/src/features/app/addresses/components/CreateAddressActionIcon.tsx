import {
    CreateAddressInput,
    createAddressInputSchema,
    useCreateAddress
} from "@/features/app/addresses/api/createAddress.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { ADDRESS_TYPE, AddressTypeKeys } from "@/utils/constants.ts";
import { ActionIcon, Button, Checkbox, Flex, Modal, Select, TextInput, Tooltip } from "@mantine/core";
import { useForm, zodResolver } from "@mantine/form";
import { notifications } from "@mantine/notifications";
import { IconCirclePlus, IconX } from "@tabler/icons-react";
import { Country } from "country-state-city";
import { useMemo, useState } from "react";

export function CreateAddressActionIcon() {
    const { user } = useAuth();
    const [modalOpened, setModalOpened] = useState(false);

    const countries = useMemo(() => Country.getAllCountries(), []);
    const countryOptions = useMemo(() => {
        return countries.map((country) => country.name);
    }, [countries]);

    const createAddressMutation = useCreateAddress({ userId: user!.id.toString() });

    const form = useForm<CreateAddressInput>({
        mode: "uncontrolled",
        initialValues: {
            name: user!.name,
            country: "",
            street: "",
            state: "",
            city: "",
            postalCode: "",
            phoneNumber: "",
            main: false,
            addressType: "HOME"
        },
        validate: zodResolver(createAddressInputSchema)
    });

    async function handleSubmit(values: typeof form.values) {
        try {
            // Don't send empty phone string
            if (values.phoneNumber === "")
                values.phoneNumber = undefined;

            await createAddressMutation.mutateAsync({ data: values });

            setModalOpened(false);
            form.reset();
            notifications.show({
                title: "Address added successfully", message: "",
                color: "teal", withBorder: true
            });
        } catch (error) {
            console.log("Error creating address", error);
            notifications.show({
                title: "Something went wrong", message: "Please try adding the address again",
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
                opened={ modalOpened } title="Add New Address" centered
                onClose={ () => {
                    setModalOpened(false);
                    form.reset();
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

                    <TextInput
                        mt="sm"
                        label="Street" required withAsterisk={ false }
                        key={ form.key("street") }
                        { ...form.getInputProps("street") }
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

                    <Checkbox
                        mt="lg"
                        label="Primary address"
                        key={ form.key("main") }
                        { ...form.getInputProps("main") }
                    />

                    <Button type="submit" mt="xl" fullWidth loading={ form.submitting }>
                        Create address
                    </Button>
                </form>
            </Modal>

            <Tooltip label="Add new address">
                <ActionIcon
                    variant="subtle" size="lg" mt={ 6 } ms={ 4 }
                    onClick={ () => {
                        setModalOpened(true);
                    } }
                >
                    <IconCirclePlus size={ 30 }/>
                </ActionIcon>
            </Tooltip>
        </>
    );
}