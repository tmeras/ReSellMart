import { useAuth } from "@/hooks/useAuth.ts";
import { Flex, Text } from "@mantine/core";
import { IconHandStop } from "@tabler/icons-react";
import { ReactNode } from "react";

export type AuthorizationProps = {
    requiresAdminRole: boolean,
    isAuthorised?: boolean;
    unauthorisedMessage?: string;
    children: ReactNode;
}

export function Authorisation({ requiresAdminRole, isAuthorised, unauthorisedMessage, children }: AuthorizationProps) {
    const { user } = useAuth();
    const userRoles = user!.roles;

    if (requiresAdminRole && !userRoles.some((role) => role.name === "ADMIN")) {
        return (
            <Flex direction="column" justify="center" align="center" h="80vh">
                <IconHandStop color="red" size={ 70 }/>

                <Text size="xl">
                    You do not have permission to view this content. Admin privileges are required.
                </Text>
            </Flex>
        );
    }

    if (isAuthorised === false) {
        return (
            <Flex direction="column" justify="center" align="center" h="80vh">
                <IconHandStop color="red" size={ 70 }/>

                <Text size="xl">
                    You do not have permission to view this content. { unauthorisedMessage && unauthorisedMessage }
                </Text>
            </Flex>
        );
    }

    return children;
}