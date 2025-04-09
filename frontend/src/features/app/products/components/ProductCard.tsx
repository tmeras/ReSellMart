import { CartButtonGroup } from "@/components/ui/CartButtonGroup.tsx";
import { CustomLink } from "@/components/ui/link/CustomLink.tsx";
import { WishlistIconButton } from "@/components/ui/WishlistIconButton.tsx";
import { paths } from "@/config/paths.ts";
import { CartItemResponse, ProductResponse } from "@/types/api.tsx";
import { PRODUCT_CONDITION } from "@/utils/constants.ts";
import { bytesToBase64, findDisplayedImage } from "@/utils/fileUtils.ts";
import { Card, Flex, Image, Text, Title } from "@mantine/core";
import { IconList, IconPackages, IconTool } from "@tabler/icons-react";

type ProductCardProps = {
    product: ProductResponse,
    cartItem: CartItemResponse | undefined, // Used to determine if product is already in cart
    inWishlist: boolean // Used to determine if product is already in wishlist
};

export function ProductCard({ product, cartItem, inWishlist }: ProductCardProps) {
    // Find the product image that should be displayed as front cover
    const displayedImage =
        findDisplayedImage(product.images)?.image ?? product.images[0].image;

    return (
        <Card withBorder radius="md" w={ 300 }>
            <Card.Section>
                <Image src={ bytesToBase64(displayedImage) } fit="contain" h={ 200 } bg="gray.4"/>
            </Card.Section>

            <Card.Section inheritPadding withBorder mt="sm">
                <Title order={ 3 }>
                    { product.name }
                </Title>
                <Text c="dimmed" size="xs" mb="sm">
                    Product #{ product.id }
                </Text>
            </Card.Section>

            <Card.Section inheritPadding withBorder mt="xs">
                <Flex direction="column" justify="start" gap={ 2 }>
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

                    <WishlistIconButton inWishlist={ inWishlist } productId={ product.id }/>
                </Flex>

                <Flex justify="space-between" align="center" mt="sm" mb="sm">
                    <CartButtonGroup cartItem={ cartItem } product={ product }/>

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