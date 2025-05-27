import { useGetCategories } from "@/api/categories/getCategories.ts";
import { DeleteModal } from "@/components/ui/DeleteModal.tsx";
import { createCategoryInputSchema, useCreateCategory } from "@/features/app/categories/api/createCategory.ts";
import { useDeleteCategory } from "@/features/app/categories/api/deleteCategory.ts";
import { updateCategoryInputSchema, useUpdateCategory } from "@/features/app/categories/api/updateCategory.ts";
import { CategoryResponse } from "@/types/api.ts";
import { FLYWAY_CATEGORIES_NUMBER } from "@/utils/constants.ts";
import { ActionIcon, Button, Flex, Tooltip } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconEdit, IconPlus, IconTrash, IconX } from "@tabler/icons-react";
import { isAxiosError } from "axios";
import {
    MantineReactTable,
    MRT_ColumnDef,
    MRT_SortingState,
    MRT_TableOptions,
    useMantineReactTable
} from "mantine-react-table";
import { useMemo, useRef, useState } from "react";
import { ZodFormattedError } from "zod";

export type CategoriesTableProps = {
    parentCategories: CategoryResponse[];
}

export function CategoriesTable({ parentCategories }: CategoriesTableProps) {
    const [globalFilter, setGlobalFilter] = useState("");
    const [sorting, setSorting] = useState<MRT_SortingState>([]);
    const [validationErrors, setValidationErrors] = useState<ZodFormattedError<CategoryResponse>>({
        _errors: [],
        id: { _errors: [] },
        name: { _errors: [] },
        parentId: { _errors: [] }
    });
    const [error, setError] = useState("");
    const [deleteModalOpened, setDeleteModalOpened] = useState(false);
    const categoryIdToDelete = useRef<string>("");

    const sortBy = sorting[0]?.id;
    const sortDirection = sorting[0]?.desc ? "desc" : "asc";

    const getCategoriesQuery = useGetCategories({
        search: globalFilter,
        sortBy,
        sortDirection
    });
    const createCategoryMutation = useCreateCategory();
    const updateCategoryMutation = useUpdateCategory();
    const deleteCategoryMutation = useDeleteCategory();

    const columns = useMemo<MRT_ColumnDef<CategoryResponse>[]>(() => [
        {
            accessorKey: "id",
            header: "ID",
            enableGrouping: false,
            enableEditing: false
        },
        {
            accessorKey: "name",
            header: "Name",
            enableGrouping: false,
            mantineEditTextInputProps: {
                error: validationErrors.name?._errors[0],
                onFocus: () =>
                    setValidationErrors({
                        ...validationErrors,
                        name: { _errors: [] }
                    })
            }
        },
        {
            accessorFn: (category) => category.parentId ?
                parentCategories?.find(parent => parent.id === category.parentId)?.name
                : "",
            id: "parentId",
            header: "Parent Category",
            enableSorting: false,
            editVariant: "select",
            mantineEditSelectProps: {
                data: parentCategories.map(parent => parent.name),
                error: validationErrors?.parentId?._errors[0],
                clearable: false,
                allowDeselect: false,
                onChange: () =>
                    setValidationErrors({
                        ...validationErrors,
                        parentId: { _errors: [] }
                    })
            }
        }
    ], [parentCategories, validationErrors]);

    function resetErrors() {
        setValidationErrors({
            _errors: [],
            id: { _errors: [] },
            name: { _errors: [] },
            parentId: { _errors: [] }
        });
        setError("");
    }

    const handleCreateCategory: MRT_TableOptions<CategoryResponse>["onCreatingRowSave"] = async function ({
                                                                                                              values,
                                                                                                              exitCreatingMode
                                                                                                          }) {
        const copiedValues = {
            ...values,
            parentId: parentCategories.find(parent => parent.name === values.parentId)?.id.toString() ?? ""
        };
        const validationResult = createCategoryInputSchema.safeParse(copiedValues);
        if (!validationResult.success) {
            setValidationErrors(validationResult.error.format());
            return;
        }

        resetErrors();
        try {
            await createCategoryMutation.mutateAsync({
                data: {
                    ...copiedValues,
                    parentId: copiedValues.parentId!
                }
            });
            exitCreatingMode();
        } catch (error) {
            console.log("Error creating category", error);
            setError("There was an error creating the category. Please try again.");
            notifications.show({
                title: "Something went wrong", message: "There was an error creating the category. Please try again.",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    };

    const handleUpdateCategory: MRT_TableOptions<CategoryResponse>["onEditingRowSave"] = async function ({
                                                                                                             values,
                                                                                                             table
                                                                                                         }) {
        const copiedValues = {
            ...values,
            parentId: parentCategories.find(parent => parent.name === values.parentId)?.id.toString() ?? ""
        };
        const validationResult = updateCategoryInputSchema.safeParse(copiedValues);
        if (!validationResult.success) {
            setValidationErrors(validationResult.error.format());
            return;
        }

        resetErrors();
        try {
            await updateCategoryMutation.mutateAsync({
                categoryId: copiedValues.id.toString(),
                data: copiedValues
            });
            table.setEditingRow(null);
        } catch (error) {
            console.log("Error updating category", error);
            setError("There was an error updating the category. Please try again.");
            notifications.show({
                title: "Something went wrong", message: "There was an error updating the category. Please try again.",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    };

    async function handleDeleteCategory({ categoryId }: { categoryId: string }) {
        try {
            console.log("delete Category", categoryId);
            await deleteCategoryMutation.mutateAsync({ categoryId });
        } catch (error) {
            console.log("Error deleting category", error);

            if (isAxiosError(error)) {
                if (error.response?.status === 409) {
                    setError("This category cannot be deleted because it has products associated with it.");
                    notifications.show({
                        title: "Conflict",
                        message: "This category cannot be deleted because it has products associated with it.",
                        color: "red",
                        icon: <IconX/>,
                        withBorder: true
                    });
                    return;
                }
            }

            setError("There was an error deleting the category. Please try again.");
            notifications.show({
                title: "Something went wrong", message: "There was an error deleting the category. Please try again.",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    const categories = getCategoriesQuery.data?.data ?? [];

    const table = useMantineReactTable({
        columns,
        data: categories,
        enableEditing: true,
        createDisplayMode: "row",
        editDisplayMode: "row",
        getRowId: (row) => row.id?.toString(),
        enablePagination: false,
        enableColumnFilters: false,
        enableGrouping: true,
        enableColumnDragging: false,
        enableMultiSort: false,
        manualFiltering: true,
        manualSorting: true,
        onGlobalFilterChange: setGlobalFilter,
        onSortingChange: setSorting,
        onCreatingRowSave: handleCreateCategory,
        onCreatingRowCancel: resetErrors,
        onEditingRowSave: handleUpdateCategory,
        onEditingRowCancel: resetErrors,
        // Only allow non-parent categories to be edited or deleted and
        // prevent deletion of categories that were inserted with database migration
        renderRowActions: ({ row, table }) => (
            row.original.parentId ? (
                <Flex gap="md">
                    <Tooltip label="Edit Category">
                        <ActionIcon
                            onClick={ () => {
                                table.setGrouping([]); // Ungroup before editing
                                table.setEditingRow(row);
                                table.setCreatingRow(null);
                                resetErrors();
                            } }
                        >
                            <IconEdit/>
                        </ActionIcon>
                    </Tooltip>

                    { row.original.id > FLYWAY_CATEGORIES_NUMBER &&
                        <Tooltip
                            label="Delete Category"
                            onClick={ () => {
                                categoryIdToDelete.current = row.original.id.toString();
                                setDeleteModalOpened(true);
                            } }
                        >
                            <ActionIcon color="red">
                                <IconTrash/>
                            </ActionIcon>
                        </Tooltip>
                    }
                </Flex>
            ) : null
        ),
        renderTopToolbarCustomActions: ({ table }) => (
            <Button
                leftSection={ <IconPlus/> }
                onClick={ () => {
                    table.setGrouping([]); // Ungroup before inserting
                    table.setCreatingRow(true);
                    table.setEditingRow(null);
                    resetErrors();
                } }
            >
                Add Category
            </Button>
        ),
        mantineToolbarAlertBannerProps: getCategoriesQuery.isError || error ?
            {
                color: "red",
                children: error ? error : "There was an error fetching categories. Please refresh and try again"
            }
            : undefined,
        initialState: {
            grouping: ["parentId"], // Group by parent category
            expanded: true,
            density: "xs"
        },
        state: {
            globalFilter,
            sorting,
            isLoading: getCategoriesQuery.isLoading,
            isSaving: createCategoryMutation.isPending || updateCategoryMutation.isPending || deleteCategoryMutation.isPending,
            showAlertBanner: getCategoriesQuery.isError || !!error,
            showProgressBars: getCategoriesQuery.isFetching,
            showGlobalFilter: true
        }
    });

    return (
        <>
            <DeleteModal
                title="Delete this category?"
                description="Are you sure you want to delete this product category? This action cannot be undone."
                opened={ deleteModalOpened } onClose={ () => setDeleteModalOpened(false) }
                onConfirm={ async () => {
                    const categoryId = categoryIdToDelete.current;
                    categoryIdToDelete.current = "";
                    await handleDeleteCategory({ categoryId });
                } }
            />

            <MantineReactTable table={ table }/>
        </>
    );
}