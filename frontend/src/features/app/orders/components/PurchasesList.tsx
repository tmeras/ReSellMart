import { useGetPurchasesByUser } from "@/features/app/orders/api/getPurchasesByUser.ts";
import { PurchaseCard } from "@/features/app/orders/components/PurchaseCard.tsx";
import { ORDER_SORT_OPTIONS, SORT_ORDERS_BY } from "@/utils/constants.ts";
import { Alert, Button, Flex, Loader, NativeSelect, Pagination, Text } from "@mantine/core";
import { IconInfoCircle } from "@tabler/icons-react";
import { useSearchParams } from "react-router";

export type PurchasesListProps = {
    userId: string;
};

export function PurchasesList({ userId }: PurchasesListProps) {
    const [searchParams, setSearchParams] = useSearchParams();
    const page = parseInt(searchParams.get("page") || "0", 10) || 0;
    const sortBy = searchParams.get("sortBy") || SORT_ORDERS_BY;
    const sortDirection = searchParams.get("sortDirection") || "desc";
    const sort = `${ sortBy } ${ sortDirection }`;
    const stripeSessionId = searchParams.get("sessionId");

    const getPurchasesByUserQuery = useGetPurchasesByUser({
        userId,
        page,
        sortBy,
        sortDirection
    });

    if (getPurchasesByUserQuery.isPending) {
        return (
            <Flex align="center" justify="center" h="100vh">
                <Loader type="bars" size="md"/>
            </Flex>
        );
    }

    if (getPurchasesByUserQuery.isError) {
        console.log("Error fetching purchases", getPurchasesByUserQuery.error);
        return (
            <Text c="red.5">
                There was an error when fetching your purchases. Please refresh and try again.
            </Text>
        );
    }

    const purchases = getPurchasesByUserQuery.data?.data.content;
    const totalPages = getPurchasesByUserQuery.data?.data.totalPages;

    if (purchases.length == 0) return <Flex justify="center ">No purchases to display</Flex>;

    // TODO: Display payment method?
    return (
        <Flex direction="column">
            <Flex justify="center" my="sm">
                <Flex
                    justify={stripeSessionId ? "space-between" : "flex-end"}
                    align="flex-end" w={ { base: 450, xs: 650 } }
                >
                    {stripeSessionId &&
                        <Alert
                            variant="light" title="New order not visible?"
                            icon={<IconInfoCircle />}
                        >
                            <Button
                                variant="light" loading={ getPurchasesByUserQuery.isFetching }
                                onClick={() => getPurchasesByUserQuery.refetch()}
                            >
                                Refresh now
                            </Button>
                        </Alert>
                    }

                    <NativeSelect
                        data={ ORDER_SORT_OPTIONS }
                        value={ sort }
                        onChange={ (e) => {
                            const selectedSort = e.currentTarget.value;
                            const [sortBy, sortDirection] = selectedSort.split(" ");

                            setSearchParams({
                                page: "0",
                                sortBy,
                                sortDirection
                            });
                        } }
                    />
                </Flex>
            </Flex>

            <Flex direction="column" gap="lg" align="center">
                { purchases.map((purchase) =>
                    <PurchaseCard key={ purchase.id } purchase={ purchase }/>
                ) }
            </Flex>

            { totalPages! > 1 &&
                <Flex align="center" justify="center" mt="xl">
                    <Pagination
                        total={ totalPages } value={ page + 1 }
                        onChange={ (p) => {
                            setSearchParams({
                                page: (p - 1).toString(),
                                sortBy,
                                sortDirection
                            });
                            window.scrollTo({ top: 0 });
                        } }
                    />
                </Flex>
            }
        </Flex>
    );
}