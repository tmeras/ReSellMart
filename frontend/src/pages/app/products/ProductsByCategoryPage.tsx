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
            <title>{ `${ category?.name } | ReSellMart` }</title>

            { category &&
                <Title ta="center" mb="md">
                    { category.name }
                </Title>
            }

            <SearchProducts
                handleSearch={ (search) => {
                    setSearch(search);
                    setPage(0);
                } }
                mb="xl" w="50%"
            />

            { (getProductsByCategoryQuery.isPending || getCategoriesQuery.isPending) &&
                <Flex align="center" justify="center" h="100vh">
                    <Loader type="bars" size="md"/>
                </Flex>
            }

            { (getProductsByCategoryQuery.isError || getCategoriesQuery.isError) &&
                <Text c="red.5">
                    There was an error when fetching the products. Please refresh and try again.
                </Text>
            }

            { products && getProductsByCategoryQuery.isSuccess &&
                <>
                    <ProductsList products={ products }/>

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
                </>
            }
        </>
    );
}