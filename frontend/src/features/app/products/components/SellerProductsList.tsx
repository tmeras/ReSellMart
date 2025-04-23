import { paths } from "@/config/paths.ts";
import { useGetProductsByUser } from "@/features/app/products/api/getProductsByUser.ts";
import { DeleteProductButton } from "@/features/app/products/components/DeleteProductButton.tsx";
import { base64ToDataUri } from "@/utils/fileUtils.ts";
import {
    ActionIcon,
    Button,
    Flex,
    Image,
    Loader,
    Pagination,
    Paper,
    Text,
    Tooltip,
    useMantineColorScheme
} from "@mantine/core";
import { IconEdit, IconEye } from "@tabler/icons-react";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router";

type SellerProductsListProps = {
    sellerId: string;
}

export function SellerProductsList({ sellerId }: SellerProductsListProps) {
    const { colorScheme } = useMantineColorScheme();
    const navigate = useNavigate();
    const [page, setPage] = useState(0);

    // TODO: Sort by latest first
    const getProductsByUserQuery = useGetProductsByUser({
        userId: sellerId,
        page
    });

    const products = getProductsByUserQuery.data?.data.content;
    const totalPages = getProductsByUserQuery.data?.data.totalPages;

    // Go back one page if current page is empty
    //TODO: TEST
    useEffect(() => {
        if (page !== 0 && products?.length === 0) {
            setPage(page - 1);
        }
    }, [page, products]);

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

    const productCards = products!.map((product) => {
        const displayedImage = product.images[0];
        const listedDateTime = new Date(product.listedAt);

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

                        <Text c="dimmed" size="sm" mt="xs">
                            { product.availableQuantity } Remaining ·
                            Listed On { listedDateTime.toLocaleDateString() }
                        </Text>

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
                <Flex direction="column" gap="xl">
                    { productCards }
                </Flex>

                { totalPages! > 1 &&
                    <Flex align="center" justify="center" mt="xl">
                        <Pagination
                            total={ totalPages! } value={ page + 1 }
                            onChange={ (p) => {
                                setPage(p - 1);
                                window.scrollTo({ top: 0 });
                            } }
                        />
                    </Flex>
                }
            </Flex>
        </>
    );
}