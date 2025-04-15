import { useGetProducts } from "@/features/app/products/api/getProducts.ts";
import { ProductsList } from "@/features/app/products/components/ProductsList.tsx";
import { SearchProducts } from "@/features/app/products/components/SearchProducts.tsx";
import { Flex, Loader, Pagination, Text, Title } from "@mantine/core";
import { useState } from "react";

export function ProductsPage() {
    const [page, setPage] = useState(0);
    const [search, setSearch] = useState("");

    const getProductsQuery = useGetProducts({ page, search });

    if (getProductsQuery.isError) console.log("Error fetching products", getProductsQuery.error);

    const products = getProductsQuery.data?.data.content;
    const totalPages = getProductsQuery.data?.data.totalPages;

    //TODO: SCROLL UP ON PAGINATION CHANGE
    return (
        <>
            <title>{ `All Products | ReSellMart` }</title>

            <Title ta="center" mb="lg">
                All Products
            </Title>

            <SearchProducts
                handleSearch={ (search) => setSearch(search) }
                mb="xl" w="50%"
            />

            { getProductsQuery.isLoading &&
                <Flex align="center" justify="center" h="100vh">
                    <Loader type="bars" size="md"/>
                </Flex>
            }

            { getProductsQuery.isError &&
                <Text c="red.5">
                    There was an error when fetching the products. Please refresh and try again.
                </Text> }

            { products && !getProductsQuery.isError &&
                <>
                    <ProductsList products={ products }/>

                    <Flex align="center" justify="center" mt="xl">
                        <Pagination total={ totalPages! } value={ page + 1 } onChange={ (p) => setPage(p - 1) }/>
                    </Flex>
                </>
            }
        </>
    );
}