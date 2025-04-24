import { useGetCategories } from "@/api/categories/getCategories.ts";
import { useGetProductsByCategory } from "@/features/app/products/api/getProductsByCategory.ts";
import { ProductsList } from "@/features/app/products/components/ProductsList.tsx";
import { SearchProducts } from "@/features/app/products/components/SearchProducts.tsx";
import { PRODUCT_SORT_OPTIONS, SORT_PRODUCTS_BY } from "@/utils/constants.ts";
import { Flex, Loader, NativeSelect, Pagination, Text, Title } from "@mantine/core";
import { useParams, useSearchParams } from "react-router";

export function ProductsByCategoryPage() {
    const params = useParams();
    const categoryId = params.categoryId as string;
    const [searchParams, setSearchParams] = useSearchParams();
    const page = parseInt(searchParams.get("page") || "0", 10) || 0;
    const search = searchParams.get("search") || "";
    const sortBy = searchParams.get("sortBy") || SORT_PRODUCTS_BY;
    const sortDirection = searchParams.get("sortDirection") || "desc";
    const sort = `${ sortBy } ${ sortDirection }`;

    const getProductsByCategoryQuery = useGetProductsByCategory({
        categoryId,
        page,
        search,
        sortBy,
        sortDirection
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
                    setSearchParams({
                        search,
                        page: "0",
                        sortBy,
                        sortDirection
                    });
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