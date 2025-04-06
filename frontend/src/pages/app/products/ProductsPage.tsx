import { useGetProducts } from "@/features/app/products/api/getProducts.ts";
import { ProductsList } from "@/features/app/products/components/ProductsList.tsx";
import { SearchProducts } from "@/features/app/products/components/SearchProducts.tsx";
import { Flex, Loader, Pagination } from "@mantine/core";
import { useState } from "react";

export function ProductsPage() {
    const [page, setPage] = useState(0);
    const [search, setSearch] = useState("");
    const [querySearch, setQuerySearch] = useState("");

    const getProductsQuery = useGetProducts({
        page,
        search: querySearch
    });

    // Only trigger search query when user has clicked on search button or pressed enter
    function handleSearch(search: string) {
        setQuerySearch(search);
    }

    if (getProductsQuery.isError) console.log("Error fetching products", getProductsQuery.error);

    const products = getProductsQuery.data?.data.content;
    const totalPages = getProductsQuery.data?.data.totalPages;

    return (
        <>
            <SearchProducts
                search={ search } setSearch={ setSearch }
                handleSearch={ handleSearch }
                mb="xl" w="50%"
            />

            { getProductsQuery.isLoading &&
                <Flex align="center" justify="center" h="100vh">
                    <Loader type="bars" size="md"/>
                </Flex>
            }

            { getProductsQuery.isError &&
                <div>There was an error when fetching products. Please try again.</div>
            }

            { products && !getProductsQuery.isError &&
                <>
                    <ProductsList products={ products }/>

                    <Flex direction="column" align="center" justify="center" mt="xl">
                        <Pagination total={ totalPages! } value={ page + 1 } onChange={ (p) => setPage(p - 1) }/>
                    </Flex>
                </>
            }
        </>
    );
}