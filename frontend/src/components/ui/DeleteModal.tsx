import { Button, Flex, Modal, Text, useMantineColorScheme } from "@mantine/core";

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
    const { colorScheme } = useMantineColorScheme();

    return (
        <Modal opened={ opened } onClose={ onClose } title={ title }>
            <Text size="sm" mt={ 10 } mb={ 20 }>
                { description }
            </Text>

            <Flex gap="md" justify="flex-end">
                <Button variant="default" onClick={ onClose }>
                    Cancel
                </Button>
                <Button onClick={ () => {
                    onConfirm();
                    onClose();
                } } color="red">
                    Delete
                </Button>
            </Flex>
        </Modal>
    );
}