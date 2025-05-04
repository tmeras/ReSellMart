import { Flex, Text } from "@mantine/core";
import { IconHandStop } from "@tabler/icons-react";
import { ReactNode } from "react";

export type AuthorizationProps = {
    isAuthorized: boolean;
    unauthorizedMessage: string;
    children: ReactNode;
}

export function Authorization({ isAuthorized, unauthorizedMessage, children }: AuthorizationProps) {
    if (isAuthorized) return children;

    return (
        <Flex direction="column" justify="center" align="center" h="80vh">
            <IconHandStop color="red" size={ 70 }/>

            <Text size="xl">
                You do not have permission to view this content. { unauthorizedMessage }
            </Text>
        </Flex>
    );
}