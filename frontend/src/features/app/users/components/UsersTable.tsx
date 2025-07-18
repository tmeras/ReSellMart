import { useGetUsers } from "@/features/app/users/api/getUsers.ts";
import { usePromoteUserToAdmin } from "@/features/app/users/api/promoteUserToAdmin.ts";
import { useUpdateUserActivation } from "@/features/app/users/api/updateUserActivation.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { AdminUserResponse } from "@/types/api.ts";
import { base64ToDataUri } from "@/utils/fileUtils.ts";
import { Avatar, Button, Flex, Text, Tooltip } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconX } from "@tabler/icons-react";
import {
    MantineReactTable,
    MRT_ColumnDef,
    MRT_PaginationState,
    MRT_SortingState,
    useMantineReactTable
} from "mantine-react-table";
import { useMemo, useState } from "react";

export function UsersTable() {
    const {user: currentUser} = useAuth();

    const [globalFilter, setGlobalFilter] = useState("");
    const [sorting, setSorting] = useState<MRT_SortingState>([]);
    const [pagination, setPagination] = useState<MRT_PaginationState>({
        pageIndex: 0,
        pageSize: 10
    });
    const [error, setError] = useState("");

    const sortBy = sorting[0]?.id;
    const sortDirection = sorting[0]?.desc ? "desc" : "asc";

    const getUsersQuery = useGetUsers({
        page: pagination.pageIndex,
        pageSize: pagination.pageSize,
        search: globalFilter,
        sortBy,
        sortDirection
    });
    const updateUserActivationMutation = useUpdateUserActivation();
    const promoteUserToAdminMutation = usePromoteUserToAdmin();

    const columns = useMemo<MRT_ColumnDef<AdminUserResponse>[]>(() => [
        {
            accessorKey: "id",
            header: "ID",
            size: 50,
            enableGrouping: false
        },
        {
            accessorKey: "name",
            header: "Name",
            enableGrouping: false,
            Cell: ({ cell }) => {
                const user = cell.row.original;
                const isCurrentUser = user.id === currentUser!.id;

                return (
                    <Flex gap="xs" align="center">
                        <Avatar
                            size={ 25 } src={ user.profileImage ? base64ToDataUri(user.profileImage) : null }
                            name={ user.name } color="initials"
                        />

                        <Text c={isCurrentUser ? "paleIndigo" : ""}>
                            { user.name } {isCurrentUser && <>(me)</>}
                        </Text>
                    </Flex>
                );
            }
        },
        {
            accessorKey: "email",
            header: "Email",
            enableGrouping: false
        },
        {
            accessorFn: (user) =>
                user.roles?.some((role) =>
                    role.name.toLowerCase() === "admin") ? "Admin" : "User",
            header: "Role",
            id: "role",
            size: 50,
            enableSorting: false
        },
        {
            accessorKey: "registeredAt",
            header: "Registered On",
            enableGrouping: false
        },
        {
            accessorKey: "isEnabled",
            header: "Status",
            size: 50,
            enableSorting: false,
            Cell: ({ cell }) =>
                <Text c={ cell.getValue<boolean>() ? "teal" : "red" }>
                    { cell.getValue<boolean>() ? "Enabled" : "Disabled" }
                </Text>
        }
    ], [currentUser]);

    async function handleEnableUser({ userId }: { userId: string }) {
        try {
            await updateUserActivationMutation.mutateAsync({
                userId,
                data: { isEnabled: true }
            });
            setError("");
        } catch (error) {
            console.log("Error enabling user:", error);

            setError("There was an error enabling the user. Please try again.");
            notifications.show({
                title: "Something went wrong", message: "There was an error enabling the user. Please try again.",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    async function handleDisableUser({ userId }: { userId: string }) {
        try {
            await updateUserActivationMutation.mutateAsync({
                userId,
                data: { isEnabled: false }
            });
            setError("");
        } catch (error) {
            console.log("Error disabling user:", error);

            setError("There was an error disabling the user. Please try again.");
            notifications.show({
                title: "Something went wrong", message: "There was an error disabling the user. Please try again.",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    async function handlePromoteUserToAdmin({ userId }: { userId: string }) {
        try {
            await promoteUserToAdminMutation.mutateAsync({ userId });
            setError("");
        } catch (error) {
            console.log("Error promoting user to admin:", error);

            setError("There was an error promoting the user to admin. Please try again.");
            notifications.show({
                title: "Something went wrong", message: "There was an error promoting the user to admin. Please try again.",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    const users = getUsersQuery.data?.data.content ?? [];
    const totalUsers = getUsersQuery.data?.data.totalElements || 0;

    const table = useMantineReactTable({
        columns,
        data: users,
        enableRowActions: true,
        positionActionsColumn: "last",
        enableColumnFilters: false,
        enableFilterMatchHighlighting: false,
        enableGrouping: true,
        enableColumnDragging: false,
        enableMultiSort: false,
        manualPagination: true,
        manualFiltering: true,
        manualSorting: true,
        onGlobalFilterChange: setGlobalFilter,
        onSortingChange: setSorting,
        onPaginationChange: setPagination,
        renderRowActions: ({ row }) => (
            <Flex gap="xs">
                { row.original.isEnabled ? (
                    <Tooltip label="This will also mark the user's products as unavailable">
                        <Button
                            color="red" size="compact-sm"
                            loading={
                                updateUserActivationMutation.isPending &&
                                updateUserActivationMutation.variables.userId === row.original.id.toString()
                            }
                            onClick={ () => handleDisableUser({ userId: row.original.id.toString() }) }
                        >
                            Disable
                        </Button>
                    </Tooltip>
                ) : (
                    <Button
                        color="teal" size="compact-sm"
                        loading={
                            updateUserActivationMutation.isPending &&
                            updateUserActivationMutation.variables.userId === row.original.id.toString()
                        }
                        onClick={ () => handleEnableUser({ userId: row.original.id.toString() }) }
                    >
                        Enable
                    </Button>
                ) }

                { !row.original.roles.some((role) => role.name.toLowerCase() === "admin") &&
                    <Tooltip label="Promote to Admin" withArrow>
                        <Button
                            size="compact-sm"
                            loading = {
                                promoteUserToAdminMutation.isPending &&
                                promoteUserToAdminMutation.variables.userId === row.original.id.toString()
                            }
                            onClick={() => handlePromoteUserToAdmin({ userId: row.original.id.toString() })}
                        >
                            Promote
                        </Button>
                    </Tooltip>
                }
            </Flex>
        ),
        mantineToolbarAlertBannerProps: getUsersQuery.isError || error ?
            {
                color: "red",
                children: error ? error : "There was an error fetching the users. Please refresh and try again"
            }
            : undefined,
        initialState: {
            grouping: ["role"], // Group by role
            expanded: true,
            density: "xs"
        },
        rowCount: totalUsers,
        state: {
            globalFilter,
            sorting,
            pagination,
            isLoading: getUsersQuery.isLoading,
            isSaving: updateUserActivationMutation.isPending || promoteUserToAdminMutation.isPending,
            showAlertBanner: getUsersQuery.isError || !!error,
            showProgressBars: getUsersQuery.isFetching
        }
    });

    return <MantineReactTable table={ table }/>;
}