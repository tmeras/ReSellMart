import { useGetProductsByCategory } from "@/features/app/products/api/getProductsByCategory.ts";
import { ProductsList } from "@/features/app/products/components/ProductsList.tsx";
import { SearchProducts } from "@/features/app/products/components/SearchProducts.tsx";
import { Flex, Loader, Pagination } from "@mantine/core";
import { useState } from "react";
import { useParams } from "react-router";

//TODO: Products by user
export function ProductsByCategoryPage() {
    const params = useParams();
    const categoryId = params.categoryId as string;

    const [page, setPage] = useState(0);
    const [search, setSearch] = useState("");
    const [querySearch, setQuerySearch] = useState("");
    const getProductsByCategoryQuery = useGetProductsByCategory({
        categoryId,
        page,
        search: querySearch
    });

    // Only trigger search query when user has clicked on search button or pressed enter
    function handleSearch(search: string) {
        setQuerySearch(search);
    }

    if (getProductsByCategoryQuery.isError) {
        console.log("Error fetching products by category", getProductsByCategoryQuery.error);
    }

    const products = getProductsByCategoryQuery.data?.data.content;
    const totalPages = getProductsByCategoryQuery.data?.data?.totalPages;

    console.log(getProductsByCategoryQuery.data);

    return (
        <>
            <SearchProducts
                search={ search } setSearch={ setSearch }
                handleSearch={ handleSearch }
                mb="xl" w="50%"
            />

            { getProductsByCategoryQuery.isLoading &&
                <Flex align="center" justify="center" h="100vh">
                    <Loader type="bars" size="md"/>
                </Flex>
            }

            { getProductsByCategoryQuery.isError &&
                <div>There was an error when fetching the products. Please try again.</div>
            }

            { products && !getProductsByCategoryQuery.isError &&
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