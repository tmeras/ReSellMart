import { DeleteModal } from "@/components/ui/DeleteModal.tsx";
import { useSoftDeleteProduct } from "@/features/app/products/api/softDeleteProduct.ts";
import { Button } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconTrash, IconX } from "@tabler/icons-react";
import { useState } from "react";

export type DeleteProductButtonProps = {
    productId: string;
    sellerId: string;
}

export function DeleteProductButton({ productId, sellerId }: DeleteProductButtonProps) {
    const [modalOpened, setModalOpened] = useState(false);

    const softDeleteProductMutation = useSoftDeleteProduct({
        productId,
        sellerId
    });

    async function deleteProduct() {
        try {
            await softDeleteProductMutation.mutateAsync({ productId });
            notifications.show({
                title: "Product successfully deleted", message: "",
                color: "teal", withBorder: true
            });
        } catch (error) {
            console.log("Error deleting product", error);
            notifications.show({
                title: "Something went wrong", message: "Please try deleting the product again",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    return (
        <>
            <DeleteModal
                title="Delete this product?"
                description="Are you sure you want to delete this product? This action cannot be undone."
                opened={ modalOpened } onClose={ () => setModalOpened(false) }
                onConfirm={ deleteProduct }
            />

            <Button
                variant="light" size="sm" color="red.5"
                leftSection={ <IconTrash size={ 18 }/> }
                onClick={ () => setModalOpened(true) }
                loading={ softDeleteProductMutation.isPending }
            >
                Delete product
            </Button>
        </>
    );
}