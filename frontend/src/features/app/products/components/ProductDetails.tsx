import { CustomLink } from "@/components/ui/link/CustomLink.tsx";
import { WishlistIconButton } from "@/components/ui/WishlistIconButton.tsx";
import { paths } from "@/config/paths.ts";
import { ProductQuantitySelect } from "@/features/app/products/components/ProductQuantitySelect.tsx";
import { useAuth } from "@/hooks/useAuth.ts";
import { CartItemResponse, ProductResponse, WishlistItemResponse } from "@/types/api.ts";
import { PRODUCT_CONDITION } from "@/utils/constants.ts";
import { base64ToDataUri } from "@/utils/fileUtils.ts";
import { Carousel } from "@mantine/carousel";
import { Divider, Flex, Image, Paper, Text, Title, useMantineColorScheme } from "@mantine/core";
import { IconList, IconTool } from "@tabler/icons-react";

export type ProductDetailsProps = {
    product: ProductResponse;
    wishlistItems: WishlistItemResponse[] | undefined;
    cartItems: CartItemResponse[] | undefined;
    wishlistEnabled: boolean;
    cartEnabled: boolean;
};

export function ProductDetails(
    { product, wishlistItems, cartItems, wishlistEnabled, cartEnabled }: ProductDetailsProps
) {
    const { colorScheme } = useMantineColorScheme();
    const { user } = useAuth();

    const isProductAvailable = !product.isDeleted && product.availableQuantity > 0;

    // Determine if product is sold by logged-in user
    const isAuthUserProduct = product.seller.id === user!.id;

    return (
            <Flex
                direction={ { base: "column", md: "row" } }
                gap="xl" my="xl" justify="center" align="center"
            >
                <Carousel
                    h={ { base: 450, md: 550 } } w={ { base: 450, md: 550 } }
                    slideGap="xl" align="center"
                    withIndicators={ product.images.length > 1 }
                    withControls={ product.images.length > 1 }
                    loop
                >
                    { product.images.map((imageResponse, index) => (
                        <Carousel.Slide key={ index }>
                            <Image
                                src={ base64ToDataUri(imageResponse.image, imageResponse.type) }
                                alt={ `Product Image ${ index + 1 }` }
                                bg={ colorScheme === "dark" ? "dark.4" : "gray.2" }
                                fit="contain" radius="md"
                                h={ { base: 450, md: 550 } } w={ { base: 450, md: 550 } }
                            />
                        </Carousel.Slide>
                    )) }
                </Carousel>

                <Flex direction="column" miw={ 450 } maw={ 600 }>
                    <Flex justify="space-between" align="center">
                        <Title> { product.name }</Title>

                        { !isAuthUserProduct &&
                            <WishlistIconButton
                                inWishlist={ !!wishlistItems?.find(wishlistItem => wishlistItem.product.id === product.id) }
                                productId={ product.id.toString() } wishlistEnabled={ wishlistEnabled } size={ 25 }
                            />
                        }
                    </Flex>

                    <Flex align="center" w="100%">
                        { !isAuthUserProduct &&
                            <Text size="sm" c="dimmed">
                                Sold by { " " }
                                <CustomLink to={ paths.app.productsByUser.getHref(product.seller.id.toString()) }>
                                    { product.seller.name }
                                </CustomLink>
                            </Text>
                        }

                        <Text size="sm" c="dimmed" ms="auto">
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
                            <Flex align="center" maw={ 350 }>
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

                    <Flex justify={ { base: "center", md: "flex-start" } }>
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
                                        cartEnabled={ cartEnabled }
                                    />
                                }
                            </Flex>
                        </Paper>
                    </Flex>
                </Flex>
            </Flex>
    );
}