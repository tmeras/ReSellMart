import { useGetUser } from "@/api/users/getUser.ts";
import imgUrl from "@/assets/user.png";
import { useGetProductsByUser } from "@/features/app/products/api/getProductsByUser.ts";
import { ProductsList } from "@/features/app/products/components/ProductsList.tsx";
import { SearchProducts } from "@/features/app/products/components/SearchProducts.tsx";
import { bytesToBase64 } from "@/utils/fileUtils.ts";
import { Avatar, Flex, Loader, Pagination, Text, Title } from "@mantine/core";
import { useState } from "react";
import { useParams } from "react-router";

export function ProductsByUserPage() {
    const params = useParams();
    const userId = params.userId as string;

    const [page, setPage] = useState(0);
    const [search, setSearch] = useState("");

    const getProductsByUserQuery = useGetProductsByUser({
        userId,
        page,
        search
    });
    const getUserQuery = useGetUser({ userId });

    // TODO: Navigate to seller's product page if user is the logged-in user

    // Only trigger search when user has clicked on search button or pressed enter
    function handleSearch(search: string) {
        setSearch(search);
    }

    if (getProductsByUserQuery.isError) {
        console.log("Error fetching products by user", getProductsByUserQuery.error);
    }

    if (getUserQuery.isError) {
        console.log("Error fetching user", getUserQuery.error);
    }

    const products = getProductsByUserQuery.data?.data.content;
    const totalPages = getProductsByUserQuery.data?.data.totalPages;
    const user = getUserQuery.data?.data;

    return (
        <>
            <title>{ `Products by ${ user?.name } | ReSellMart` }</title>

            { user &&
                <Flex justify="center" align="center" mb="md">
                    <Title me="sm">
                        Products by { user.name }
                    </Title>
                    <Avatar
                        size={ 45 }
                        src={ user.profileImage ? bytesToBase64(user.profileImage) : imgUrl }
                    />
                </Flex>
            }

            <SearchProducts
                handleSearch={ handleSearch }
                mb="xl" w="50%"
            />

            { (getProductsByUserQuery.isPending || getUserQuery.isPending) &&
                <Flex align="center" justify="center" h="100vh">
                    <Loader type="bars" size="md"/>
                </Flex>
            }

            { (getProductsByUserQuery.isError || getUserQuery.isError) &&
                <Text c="red.5">
                    There was an error when fetching the products. Please refresh and try again.
                </Text>
            }

            { products && !getProductsByUserQuery.isError &&
                <>
                    <ProductsList products={ products }/>

                    <Flex align="center" justify="center" mt="xl">
                        <Pagination
                            total={ totalPages! } value={ page + 1 }
                            onChange={ (p) => {
                                setPage(p - 1);
                                window.scrollTo({ top: 0 });
                            } }
                        />
                    </Flex>
                </>
            }
        </>
    );
}