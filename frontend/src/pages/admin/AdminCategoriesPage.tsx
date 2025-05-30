import { Authorisation } from "@/components/Authorisation.tsx";
import { useGetParentCategories } from "@/features/app/categories/api/getParentCategories";
import { CategoriesTable } from "@/features/app/categories/components/CategoriesTable.tsx";
import { Flex, Loader, Text, Title } from "@mantine/core";

export function AdminCategoriesPage() {

    const getParentCategoriesQuery = useGetParentCategories();

    if (getParentCategoriesQuery.isPending) {
        return (
            <Flex align="center" justify="center" h="100vh">
                <Loader type="bars" size="md"/>
            </Flex>
        );
    }

    if (getParentCategoriesQuery.isError) {
        console.log("Error fetching parent categories", getParentCategoriesQuery.error);
        return (
            <Text c="red.5">
                There was an error when fetching categories. Please refresh and try again.
            </Text>
        );
    }

    return (
        <>
            <Authorisation requiresAdminRole={ true }>
                <title>{ `Manage Categories | ReSellMart` }</title>

                <Title ta="center" mb="md">
                    Product Categories
                </Title>

                <CategoriesTable parentCategories={ getParentCategoriesQuery.data.data }/>
            </Authorisation>
        </>
    );
}