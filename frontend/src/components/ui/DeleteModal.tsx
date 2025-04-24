import { Button, Flex, Modal, Text } from "@mantine/core";

export type DeleteModalProps = {
    opened: boolean;
    onClose: () => void;
    onConfirm: () => void;
    title: string;
    description: string;
}

export function DeleteModal(
    { opened, onClose, onConfirm, title, description }: DeleteModalProps
) {

    return (
        <Modal opened={ opened } onClose={ onClose } title={ title }>
            <Text size="sm" mt={ 10 } mb={ 20 }>
                { description }
            </Text>

            <Flex gap="md" justify="flex-end">
                <Button variant="default" onClick={ onClose }>
                    Cancel
                </Button>
                <Button
                    color="red" data-autofocus
                    onClick={ () => {
                        onConfirm();
                        onClose();
                    } }
                >
                    Delete
                </Button>
            </Flex>
        </Modal>
    );
}