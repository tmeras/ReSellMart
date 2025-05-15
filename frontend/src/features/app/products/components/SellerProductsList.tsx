import { paths } from "@/config/paths.ts";
import { useGetProductsByUser } from "@/features/app/products/api/getProductsByUser.ts";
import { DeleteProductButton } from "@/features/app/products/components/DeleteProductButton.tsx";
import { SORT_PRODUCTS_BY } from "@/utils/constants.ts";
import { base64ToDataUri } from "@/utils/fileUtils.ts";
import {
    ActionIcon,
    Button,
    Flex,
    Image,
    Loader,
    NativeSelect,
    Pagination,
    Paper,
    Text,
    Tooltip,
    useMantineColorScheme
} from "@mantine/core";
import { IconEdit, IconEye } from "@tabler/icons-react";
import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router";

type SellerProductsListProps = {
    sellerId: string;
}

export function SellerProductsList({ sellerId }: SellerProductsListProps) {
    const [searchParams, setSearchParams] = useSearchParams();
    const page = parseInt(searchParams.get("page") || "0", 10) || 0;
    const sortBy = searchParams.get("sortBy") || SORT_PRODUCTS_BY;
    const sortDirection = searchParams.get("sortDirection") || "desc";
    const sort = `${ sortBy } ${ sortDirection }`;

    const { colorScheme } = useMantineColorScheme();
    const navigate = useNavigate();

    const getProductsByUserQuery = useGetProductsByUser({
        userId: sellerId,
        page,
        search: "",
        sortBy,
        sortDirection
    });

    const products = getProductsByUserQuery.data?.data.content;
    const totalPages = getProductsByUserQuery.data?.data.totalPages;

    // Go back one page if current page is empty
    useEffect(() => {
        if (!getProductsByUserQuery.isFetching && page !== 0 && products?.length === 0) {
            setSearchParams((prev) => new URLSearchParams({
                page: (page - 1).toString(),
                sortBy: prev.get("sortBy") || SORT_PRODUCTS_BY,
                sortDirection: prev.get("sortDirection") || "desc"
            }));
        }
    }, [getProductsByUserQuery.isFetching, page, products, setSearchParams]);

    if (getProductsByUserQuery.isPending) {
        return (
            <Flex align="center" justify="center" h="100vh">
                <Loader type="bars" size="md"/>
            </Flex>
        );
    }

    if (getProductsByUserQuery.isError) {
        console.log("Error fetching seller's products", getProductsByUserQuery.error);
        return (
            <Text c="red.5">
                There was an error when fetching your products. Please refresh and try again.
            </Text>
        );
    }

    if (products!.length === 0) return <div>No products to display</div>;

    const sortOptions = [
        {
            value: "listedAt desc",
            label: "Sort by: Date listed (descending)"
        },
        {
            value: "listedAt asc",
            label: "Sort by: Date listed (ascending)"
        }
    ];

    const productCards = products!.map((product) => {
        const displayedImage = product.images[0];
        const listedAt = new Date(product.listedAt);

        return (
            <Paper
                key={ product.id } withBorder
                p="md" radius="sm"
                w={ { base: 480, xs: 550, md: 800 } }
            >
                <Flex gap="md">
                    <Flex align="center">
                        <Image
                            src={ base64ToDataUri(displayedImage.image, displayedImage.type) } alt="Product Image"
                            fit="contain" h={ 170 } bg={ colorScheme === "dark" ? "dark.4" : "gray.2" }
                            miw={ { base: 110, xs: 150, md: 200 } } maw={ { base: 110, xs: 150, md: 200 } }
                            style={ product.availableQuantity > 0 ? {} : {
                                filter: "blur(2px) grayscale(50%) brightness(70%)",
                                opacity: 0.7
                            } }
                        />
                    </Flex>

                    <Flex direction="column" w="100%" justify="center">
                        <Text c="dimmed" size="xs" ms="auto">
                            Product #{ product.id }
                        </Text>

                        <Flex>
                            <Text size="xl" fw={ 700 } maw="85%">
                                { product.name }
                            </Text>
                            <Tooltip label="Preview product">
                                <ActionIcon
                                    variant="subtle" size="compact-sm" ms="xs"
                                    onClick={ () =>
                                        navigate(paths.app.productDetails.getHref(product.id.toString()))
                                    }
                                >
                                    <IconEye size={ 18 }/>
                                </ActionIcon>
                            </Tooltip>
                        </Flex>

                        <Flex mt="xs">
                            { product.availableQuantity > 0 ? (
                                <Text size="sm" c="dimmed">
                                    { product.availableQuantity } remaining
                                </Text>
                            ) : (
                                <Text c="red.5" size="sm">
                                    Out of stock
                                </Text>
                            ) }
                            <Text size="sm" c="dimmed" ms={ 5 }>
                                · Listed On { listedAt.toLocaleDateString() }
                            </Text>
                        </Flex>

                        <Flex mt="xl" gap="sm">
                            <Button
                                variant="light" size="sm"
                                leftSection={ <IconEdit size={ 18 }/> }
                                onClick={ () =>
                                    navigate(paths.app.updateProduct.getHref(product.id.toString()))
                                }
                            >
                                Edit product
                            </Button>

                            <DeleteProductButton productId={ product.id.toString() } sellerId={ sellerId }/>
                        </Flex>
                    </Flex>
                </Flex>
            </Paper>
        );
    });

    return (
        <>
            <Flex direction="column">
                <Flex justify="flex-end">
                    <NativeSelect
                        data={ sortOptions }
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

                <Flex direction="column" gap="xl" mt="sm">
                    { productCards }
                </Flex>

                { totalPages! > 1 &&
                    <Flex align="center" justify="center" mt="xl">
                        <Pagination
                            total={ totalPages! } value={ page + 1 }
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
        </>
    );
}