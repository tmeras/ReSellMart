import { useGetCart } from "@/api/cart/getCart.ts";
import { useGetWishlist } from "@/api/wishlist/getWishlist.ts";
import { CustomLink } from "@/components/ui/link/CustomLink.tsx";
import { WishlistIconButton } from "@/components/ui/WishlistIconButton.tsx";
import { paths } from "@/config/paths.ts";
import { ProductQuantitySelect } from "@/features/app/products/components/ProductQuantitySelect.tsx";
import { SimilarProducts } from "@/features/app/products/components/SimilarProducts.tsx";
import { useAuth } from "@/hooks/useAuth.ts";
import { ProductResponse } from "@/types/api.tsx";
import { PRODUCT_CONDITION } from "@/utils/constants.ts";
import { bytesToBase64 } from "@/utils/fileUtils.ts";
import { Carousel } from "@mantine/carousel";
import { Divider, Flex, Image, Paper, Text, Title, useMantineColorScheme } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconList, IconTool, IconX } from "@tabler/icons-react";
import { useEffect } from "react";

export type ProductDetailsProps = {
    product: ProductResponse;
};

export function ProductDetails({ product }: ProductDetailsProps) {
    const { colorScheme } = useMantineColorScheme();
    const { user } = useAuth();

    const getWishlistQuery = useGetWishlist({ userId: user!.id });
    const getCartQuery = useGetCart({ userId: user!.id });

    useEffect(() => {
        if (getWishlistQuery.isError) {
            console.log("Get wishlist error", getWishlistQuery.error);
            notifications.show({
                title: "Could not fetch wishlist", message: "Please refresh and try again",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }, [getWishlistQuery.isError, getWishlistQuery.error]);

    useEffect(() => {
        if (getCartQuery.isError) {
            console.log("Get cart error", getCartQuery.error);
            notifications.show({
                title: "Could not fetch cart", message: "Please refresh and try again",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }, [getCartQuery.isError, getCartQuery.error]);

    const wishlistItems = getWishlistQuery.data?.data;
    const cartItems = getCartQuery.data?.data;
    const isProductAvailable = !product.deleted && product.availableQuantity > 0;

    // Determine if product is sold by logged-in user
    const isAuthUserProduct = product.seller.id === user!.id;

    return (
        <Flex direction="column" mih="100vh" w="100%" p="md">
            <Flex
                direction={ { base: "column", md: "row" } }
                gap="xl" my="xl" justify="center" align="center"
            >
                <Carousel
                    h={ { base: 450, md: 550 } } w={ { base: 450, md: 550 } }
                    slideGap="xl" align="center"
                    withIndicators loop
                >
                    { product.images.map((imageResponse, index) => (
                        <Carousel.Slide key={ index }>
                            <Image
                                src={ bytesToBase64(imageResponse.image) } alt={ `Product Image ${ index + 1 }` }
                                bg={ colorScheme === "dark" ? "dark.4" : "gray.2" }
                                fit="contain" radius="md"
                                h={ { base: 450, md: 550 } } w={ { base: 450, md: 550 } }
                            />
                        </Carousel.Slide>
                    )) }
                </Carousel>

                <Flex direction="column" miw={ 450 } maw={ 600 }>
                    <Flex justify="space-between" align="center">
                        <Title order={ 1 }> { product.name }</Title>

                        { !isAuthUserProduct &&
                            <WishlistIconButton
                                inWishlist={ !!wishlistItems?.find(wishlistItem => wishlistItem.product.id === product.id) }
                                productId={ product.id } wishlistEnabled={ getWishlistQuery.isSuccess } size={ 25 }
                            />
                        }
                    </Flex>

                    <Flex justify="space-between" align="center">
                        <Text size="sm" c="dimmed">
                            Sold by { " " }
                            <CustomLink to={ paths.app.productsByUser.getHref(product.seller.id.toString()) }>
                                { product.seller.name }
                            </CustomLink>
                        </Text>

                        <Text size="sm" c="dimmed">
                            Product #{ product.id }
                        </Text>
                    </Flex>

                    <Divider my="md"/>

                    <Title order={ 3 }>
                        About this item
                    </Title>

                    <Text size="lg">
                        { product.description }
                    </Text>

                    <Divider my="md"/>

                    <Flex direction="column" gap={ 2 }>
                        <Flex justify="space-between" mb={ 2 }>
                            <Flex align="center">
                                <IconList size={ 20 }/>
                                <Text span fw={ 700 } size="md" ms={ 9 } me={ 3 }>
                                    Category:
                                </Text>
                                <Text span size="md">
                                    { product.category.name }
                                </Text>
                            </Flex>

                            <Flex align="center">
                                <IconTool size={ 20 }/>
                                <Text span fw={ 700 } size="md" ms={ 9 } me={ 3 }>
                                    Condition:
                                </Text>
                                <Text span size="md">
                                    { PRODUCT_CONDITION[product.productCondition] }
                                </Text>
                            </Flex>
                        </Flex>
                    </Flex>

                    <Paper withBorder radius="md" mt="xl" p="md" w="60%">
                        <Flex direction="column" gap="xs">
                            <Flex align="center">
                                <Text span size="xl" fw={ 500 }>
                                    £ { product.price.toFixed(2) }
                                </Text>
                                { product.previousPrice && product.previousPrice > product.price &&
                                    <Text span size="lg" fw={ 300 } ms="xs" td="line-through">
                                        { product.previousPrice.toFixed(2) }
                                    </Text>
                                }
                            </Flex>

                            { !isProductAvailable ? (
                                <Text c="red.5" size="md">
                                    This product is no longer available
                                </Text>
                            ) : product.availableQuantity > 3 ? (
                                <Text c="teal.6" size="md">
                                    In Stock
                                </Text>
                            ) : (
                                <Text c="orange.6">
                                    Only { product.availableQuantity } left in stock!
                                </Text>
                            ) }

                            { isProductAvailable && !isAuthUserProduct &&
                                <ProductQuantitySelect
                                    product={ product }
                                    cartItem={ cartItems?.find(cartItem => cartItem.product.id === product.id) }
                                    cartEnabled={ getCartQuery.isSuccess }
                                />
                            }
                        </Flex>
                    </Paper>
                </Flex>
            </Flex>

            <Divider size="md" my="xl"/>

            <SimilarProducts
                product={ product }
                wishlistItems={ wishlistItems } cartItems={ cartItems }
                wishlistEnabled={ getWishlistQuery.isSuccess } cartEnabled={ getCartQuery.isSuccess }
            />
        </Flex>
    );
}