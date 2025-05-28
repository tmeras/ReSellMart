import { Authorisation } from "@/components/Authorisation.tsx";
import { UsersTable } from "@/features/app/users/components/UsersTable.tsx";
import { Title } from "@mantine/core";

export function UsersPage() {

    return (
        <>
            <Authorisation requiresAdminRole={ true }>
                <title>{ `Manage Users | ReSellMart` }</title>

                <Title ta="center" mb="md">
                    Users
                </Title>

                <UsersTable/>
            </Authorisation>
        </>
    );
}