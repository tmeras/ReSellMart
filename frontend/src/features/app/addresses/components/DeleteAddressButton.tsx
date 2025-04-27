import { DeleteModal } from "@/components/ui/DeleteModal.tsx";
import { useDeleteAddress } from "@/features/app/addresses/api/deleteAddress.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { Button } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconTrash, IconX } from "@tabler/icons-react";
import { useState } from "react";

export type DeleteAddressButtonProps = {
    addressId: string;
};

export function DeleteAddressButton({ addressId }: DeleteAddressButtonProps) {
    const { user } = useAuth();
    const [modalOpened, setModalOpened] = useState(false);

    const deleteAddressMutation = useDeleteAddress({ userId: user!.id.toString() });

    async function deleteAddress() {
        try {
            await deleteAddressMutation.mutateAsync({ addressId });
        } catch (error) {
            console.log("Error deleting address", error);
            notifications.show({
                title: "Something went wrong", message: "Please try deleting the address again",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    return (
        <>
            <DeleteModal
                title="Delete this address?"
                description="Are you sure you want to delete this address? This action cannot be undone."
                opened={ modalOpened } onClose={ () => setModalOpened(false) }
                onConfirm={ deleteAddress }
            />

            <Button
                variant="light" size="compact-sm" color="red.5"
                leftSection={ <IconTrash size={ 16 }/> }
                onClick={ () => setModalOpened(true) }
                loading={ deleteAddressMutation.isPending }
            >
                Delete
            </Button>
        </>
    );
}