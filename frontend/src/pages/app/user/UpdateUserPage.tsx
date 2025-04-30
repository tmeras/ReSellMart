import { UpdateUserForm } from "@/features/app/users/components/UpdateUserForm.tsx";
import { Title } from "@mantine/core";

export function UpdateUserPage() {
    return (
        <>
            <title>Account Settings</title>

            <Title ta="center">
                Account Settings
            </Title>

            <UpdateUserForm/>
        </>
    );

}