import { CartButtonGroup } from "@/components/ui/CartButtonGroup.tsx";
import { CustomLink } from "@/components/ui/link/CustomLink.tsx";
import { WishlistActionIcon } from "@/components/ui/WishlistActionIcon.tsx";
import { paths } from "@/config/paths.ts";
import { CartItemResponse, ProductResponse } from "@/types/api.ts";
import { PRODUCT_CONDITION } from "@/utils/constants.ts";
import { base64ToDataUri } from "@/utils/fileUtils.ts";
import { Anchor, Card, Flex, Image, Text, useMantineColorScheme } from "@mantine/core";
import { IconList, IconPackages, IconTool } from "@tabler/icons-react";
import { Link, useNavigate } from "react-router";

type ProductCardProps = {
    product: ProductResponse,
    cartItem: CartItemResponse | undefined, // Used to determine if product is already in cart
    inWishlist: boolean // Used to determine if product is already in wishlist
    cartEnabled: boolean
    wishlistEnabled: boolean
};

export function ProductCard(
    { product, cartItem, inWishlist, cartEnabled, wishlistEnabled }: ProductCardProps
) {
    const { colorScheme } = useMantineColorScheme();
    const navigate = useNavigate();

    // Find the product image that should be displayed as front cover
    const displayedImage = product.images[0];

    return (
        <Card withBorder radius="md" w={ 300 }>
            <Card.Section>
                <Image
                    src={ base64ToDataUri(displayedImage.image, displayedImage.type) } alt="Product Image"
                    fit="contain" h={ 200 }
                    bg={ colorScheme === "dark" ? "dark.4" : "gray.2" }
                    style={ { cursor: "pointer" } }
                    onClick={ () => navigate(paths.app.productDetails.getHref(product.id.toString())) }
                />
            </Card.Section>

            <Card.Section inheritPadding withBorder mt="sm">
                <Anchor
                    size="xl" lineClamp={ 2 }
                    c="var(--mantine-color-text)"
                    component={ Link } to={ paths.app.productDetails.getHref(product.id.toString()) }
                >
                    { product.name }
                </Anchor>
                <Text c="dimmed" size="xs" mb="sm">
                    Product #{ product.id }
                </Text>
            </Card.Section>

            <Card.Section inheritPadding withBorder mt="xs">
                <Flex direction="column" gap={ 2 }>
                    <Flex justify="space-between" mb={ 2 }>
                        <Flex align="center">
                            <IconList size={ 16 }/>
                            <Text span size="sm" ms={ 5 }>
                                { product.category.name }
                            </Text>
                        </Flex>

                        <Flex align="center">
                            <IconTool size={ 16 }/>
                            <Text span size="sm" ms={ 5 }>
                                { PRODUCT_CONDITION[product.productCondition] }
                            </Text>
                        </Flex>
                    </Flex>

                    <Flex align="center" c={ product.availableQuantity <= 3 ? "red.6" : "" } mb="sm">
                        <IconPackages size={ 16 }/>
                        <Text span size="sm" ms={ 5 }>
                            Available Quantity: { product.availableQuantity }
                        </Text>
                    </Flex>
                </Flex>
            </Card.Section>

            <Card.Section inheritPadding mt="xs">
                <Flex justify="space-between" align="center">
                    <div>
                        <Text span size="xl" fw={ 500 }>
                            £ { product.price.toFixed(2) }
                        </Text>
                        { product.previousPrice && product.previousPrice > product.price &&
                            <Text span size="md" fw={ 300 } ms="xs" td="line-through">
                                { product.previousPrice.toFixed(2) }
                            </Text>
                        }
                    </div>

                    <WishlistActionIcon
                        inWishlist={ inWishlist } productId={ product.id.toString() }
                        wishlistEnabled={ wishlistEnabled }
                        size={ 20 }
                    />
                </Flex>

                <Flex justify="space-between" align="center" mt="sm" mb="sm">
                    <CartButtonGroup
                        cartItem={ cartItem } product={ product } cartEnabled={ cartEnabled }
                    />

                    <Text size="xs" c="dimmed">
                        Sold by { " " }
                        <CustomLink to={ paths.app.productsByUser.getHref(product.seller.id.toString()) }>
                            { product.seller.name }
                        </CustomLink>
                    </Text>
                </Flex>
            </Card.Section>
        </Card>
    );
}