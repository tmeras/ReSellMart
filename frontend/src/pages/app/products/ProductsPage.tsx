import { useGetProducts } from "@/features/app/products/api/getProducts.ts";
import { ProductsList } from "@/features/app/products/components/ProductsList.tsx";
import { SearchProducts } from "@/features/app/products/components/SearchProducts.tsx";
import { PRODUCT_SORT_OPTIONS, SORT_PRODUCTS_BY } from "@/utils/constants.ts";
import { Flex, Loader, NativeSelect, Pagination, Text, Title } from "@mantine/core";
import { useSearchParams } from "react-router";

export function ProductsPage() {
    const [searchParams, setSearchParams] = useSearchParams();
    const page = parseInt(searchParams.get("page") || "0", 10) || 0;
    const search = searchParams.get("search") || "";
    const sortBy = searchParams.get("sortBy") || SORT_PRODUCTS_BY;
    const sortDirection = searchParams.get("sortDirection") || "desc";
    const sort = `${ sortBy } ${ sortDirection }`;

    const getProductsQuery = useGetProducts({
        page,
        search,
        sortBy,
        sortDirection
    });

    if (getProductsQuery.isError) console.log("Error fetching products", getProductsQuery.error);

    const products = getProductsQuery.data?.data.content;
    const totalPages = getProductsQuery.data?.data.totalPages;

    return (
        <>
            <title>{ `All Products | ReSellMart` }</title>

            <Title ta="center" mb="lg">
                All Products
            </Title>

            <SearchProducts
                handleSearch={ (search) => {
                    setSearchParams({
                        search,
                        page: "0",
                        sortBy,
                        sortDirection
                    });
                } }
                mb="xl" w="50%"
            />

            { getProductsQuery.isPending &&
                <Flex align="center" justify="center" h="100vh">
                    <Loader type="bars" size="md"/>
                </Flex>
            }

            { getProductsQuery.isError &&
                <Text c="red.5">
                    There was an error when fetching the products. Please refresh and try again.
                </Text>
            }

            { products && getProductsQuery.isSuccess &&
                <>
                    <Flex justify={ { base: "center", sm: "flex-end" } } mb="sm">
                        <NativeSelect
                            data={ PRODUCT_SORT_OPTIONS }
                            value={ sort }
                            onChange={ (e) => {
                                const selectedSort = e.currentTarget.value;
                                const [sortBy, sortDirection] = selectedSort.split(" ");

                                setSearchParams({
                                    search,
                                    page: "0",
                                    sortBy,
                                    sortDirection
                                });
                            } }
                        />
                    </Flex>

                    <ProductsList products={ products }/>

                    { totalPages! > 1 &&
                        <Flex align="center" justify="center" mt="xl">
                            <Pagination
                                total={ totalPages! } value={ page + 1 }
                                onChange={ (p) => {
                                    setSearchParams({
                                        search,
                                        page: (p - 1).toString(),
                                        sortBy,
                                        sortDirection
                                    });
                                    window.scrollTo({ top: 0 });
                                } }
                            />
                        </Flex>
                    }
                </>
            }
        </>
    );
}