import { useGetCategories } from "@/api/categories/getCategories.ts";
import { useGetProductsByCategory } from "@/features/app/products/api/getProductsByCategory.ts";
import { ProductsList } from "@/features/app/products/components/ProductsList.tsx";
import { SearchProducts } from "@/features/app/products/components/SearchProducts.tsx";
import { Flex, Loader, Pagination, Text, Title } from "@mantine/core";
import { useState } from "react";
import { useParams } from "react-router";

export function ProductsByCategoryPage() {
    const params = useParams();
    const categoryId = params.categoryId as string;

    const [page, setPage] = useState(0);
    const [search, setSearch] = useState("");
    const getProductsByCategoryQuery = useGetProductsByCategory({
        categoryId,
        page,
        search
    });
    // Use cached categories that were previously fetched to be displayed in the navbar
    const getCategoriesQuery = useGetCategories();

    // Only trigger search query when user has clicked on search button or pressed enter
    function handleSearch(search: string) {
        setSearch(search);
    }

    if (getProductsByCategoryQuery.isError) {
        console.log("Error fetching products by category", getProductsByCategoryQuery.error);
    }

    if (getCategoriesQuery.isError) {
        console.log("Error fetching categories", getCategoriesQuery.error);
    }

    const products = getProductsByCategoryQuery.data?.data.content;
    const totalPages = getProductsByCategoryQuery.data?.data?.totalPages;
    const category = getCategoriesQuery.data?.data
        ?.find(category => category.id.toString() === categoryId);

    return (
        <>
            { category &&
                <Title ta="center" mb="md">
                    { category.name }
                </Title>
            }

            <SearchProducts
                handleSearch={ handleSearch }
                mb="lg" w="50%"
            />

            { getProductsByCategoryQuery.isLoading &&
                <Flex align="center" justify="center" h="100vh">
                    <Loader type="bars" size="md"/>
                </Flex>
            }

            { getProductsByCategoryQuery.isError &&
                <Text c="red.5">
                    There was an error when fetching the products. Please try again.
                </Text>
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