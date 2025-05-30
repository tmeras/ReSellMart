import { useGetProducts } from "@/features/app/products/api/getProducts.ts";
import { useUpdateProductAvailability } from "@/features/app/products/api/updateProductAvailability.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { ProductResponse } from "@/types/api.ts";
import { base64ToDataUri } from "@/utils/fileUtils.ts";
import { Avatar, Button, Flex, Image, Text, Tooltip, useMantineColorScheme } from "@mantine/core";
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

export function ProductsTable() {
    const {colorScheme} = useMantineColorScheme();
    const {user: currentUser} = useAuth();

    const [globalFilter, setGlobalFilter] = useState("");
    const [sorting, setSorting] = useState<MRT_SortingState>([
        {
            id: "listedAt",
            desc: true
        }
    ]);
    const [pagination, setPagination] = useState<MRT_PaginationState>({
        pageIndex: 0,
        pageSize: 10
    });
    const [error, setError] = useState("");

    const sortBy = sorting[0]?.id;
    const sortDirection = sorting[0]?.desc ? "desc" : "asc";

    const getProductsQuery = useGetProducts({
        page: pagination.pageIndex,
        pageSize: pagination.pageSize,
        search: globalFilter,
        sortBy,
        sortDirection
    });
    const updateProductAvailabilityMutation = useUpdateProductAvailability();

    const columns = useMemo<MRT_ColumnDef<ProductResponse>[]>(() => [
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
                const product = cell.row.original;
                const displayedImage = product.images[0]; // Find the product image that should be displayed as front cover

                return (
                    <Flex gap="xs" align="center">
                        <Image
                            src={ base64ToDataUri(displayedImage.image, displayedImage.type) } alt="Product Image"
                            fit="contain" h={80} miw={80} maw={80}
                            bg={ colorScheme === "dark" ? "dark.4" : "gray.2" }
                        />

                        <Text>
                            { product.name }
                        </Text>
                    </Flex>
                );
            },

        },
        {
            accessorFn: (product) =>  "£" +  product.price?.toFixed(2),
            header: "Price",
            id: "price",
            enableGrouping: false,
            size: 110
        },
        {
            accessorKey: "availableQuantity",
            header: "Available Qty.",
            enableGrouping: false,
            size: 100
        },
        {
            accessorFn: (product) => product.seller?.name,
            header: "Seller",
            id: "seller.name",
            Cell: ({ cell }) => {
                const product = cell.row.original;
                const seller = product.seller;
                const isCurrentUser = seller?.id === currentUser!.id;

                return (
                    <Flex gap="xs" align="center">
                        <Avatar
                            src={ seller.profileImage ? base64ToDataUri(seller.profileImage) : null }
                            size={ 25 } name={ seller.name } color="initials"
                        />

                        <Text c={isCurrentUser ? "paleIndigo" : ""}>
                            { seller.name } {isCurrentUser && <>(me)</>}
                        </Text>
                    </Flex>
                );
            }
        },
        {
            accessorKey: "listedAt",
            header: "Listed At (UTC)",
            enableGrouping: false,
            Cell: ({ cell }) =>
                <Text>
                    { new Date(cell.getValue<string>()).toLocaleString("en-GB", {
                        dateStyle: "short",
                        timeStyle: "short",
                        timeZone: "UTC"
                    }) }
                </Text>
        },
        {
            accessorKey: "isDeleted",
            header: "Status",
            size: 50,
            enableSorting: false,
            Cell: ({ cell }) =>
                <Text c={ cell.getValue<boolean>() ? "red" : "teal" }>
                    { cell.getValue<boolean>() ? "Unavailable" : "Available" }
                </Text>
        }
    ], [colorScheme, currentUser]);

    async function handleMakeProductAvailable({ productId }: { productId: string }) {
        try {
            await updateProductAvailabilityMutation.mutateAsync({
                productId,
                data: { isDeleted: false }
            });
            setError("");
        } catch(error){
            console.log("Error making product available:", error);

            setError("There was an error making the product available. Please try again.");
            notifications.show({
                title: "Something went wrong", message: "There was an error making the product available. Please try again.",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    async function handleMakeProductUnavailable({ productId }: { productId: string }) {
        try {
            await updateProductAvailabilityMutation.mutateAsync({
                productId,
                data: { isDeleted: true }
            });
            setError("");
        } catch(error){
            console.log("Error making product unavailable:", error);

            setError("There was an error making the product unavailable. Please try again.");
            notifications.show({
                title: "Something went wrong", message: "There was an error making the product unavailable. Please try again.",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    const products = getProductsQuery.data?.data.content || [];
    const totalProducts = getProductsQuery.data?.data.totalElements || 0;

    const table = useMantineReactTable({
        columns,
        data: products,
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
        renderRowActions: ({row}) => (
          row.original.isDeleted ? (
              <Tooltip label="Make this product visible to other users" withArrow>
                  <Button
                      color="teal" size="compact-sm"
                      loading={
                          updateProductAvailabilityMutation.isPending &&
                          updateProductAvailabilityMutation.variables.productId === row.original.id.toString()
                      }
                      onClick={ () => handleMakeProductAvailable({ productId: row.original.id.toString() }) }
                  >
                      Make Available
                  </Button>
              </Tooltip>
          ) : (
              <Tooltip label="Hide this product from other users" withArrow>
                  <Button
                      color="red" size="compact-sm"
                      loading={
                          updateProductAvailabilityMutation.isPending &&
                          updateProductAvailabilityMutation.variables.productId === row.original.id.toString()
                      }
                      onClick={ () => handleMakeProductUnavailable({ productId: row.original.id.toString() }) }
                  >
                      Make Unavailable
                  </Button>
              </Tooltip>
        )),
        mantineToolbarAlertBannerProps: getProductsQuery.isError || error ?
            {
                color: "red",
                children: error ? error : "There was an error fetching the users. Please refresh and try again"
            }
            : undefined,
        initialState: {
            expanded: true,
            density: "xs"
        },
        rowCount: totalProducts,
        state: {
            globalFilter,
            sorting,
            pagination,
            isLoading: getProductsQuery.isLoading,
            isSaving: updateProductAvailabilityMutation.isPending,
            showAlertBanner: getProductsQuery.isError || !!error,
            showProgressBars: getProductsQuery.isFetching
        }
    });

    return <MantineReactTable table={table} />;
}