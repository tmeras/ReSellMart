import { CartButtonGroup } from "@/components/ui/CartButtonGroup.tsx";
import { CustomLink } from "@/components/ui/link/CustomLink.tsx";
import { paths } from "@/config/paths.ts";
import { DeleteWishlistItemActionIcon } from "@/features/app/wishlist/components/DeleteWishlistItemActionIcon.tsx";
import { CartItemResponse, WishlistItemResponse } from "@/types/api.ts";
import { base64ToDataUri } from "@/utils/fileUtils.ts";
import { Anchor, Flex, Image, Paper, Text, useMantineColorScheme } from "@mantine/core";
import { Link, useNavigate } from "react-router";

type WishlistItemCardProps = {
    wishlistItem: WishlistItemResponse;
    cartItem: CartItemResponse | undefined; // Used to determine if product is already in cart
    cartEnabled: boolean;
}

export function WishlistItemCard({ wishlistItem, cartItem, cartEnabled }: WishlistItemCardProps) {
    const { colorScheme } = useMantineColorScheme();
    const navigate = useNavigate();

    const product = wishlistItem.product;
    const displayedImage = product.images[0];
    const addedAt = new Date(wishlistItem.addedAt);
    const isProductAvailable = !product.isDeleted && product.availableQuantity > 0;

    return (
        <Paper
            key={ product.id } withBorder
            p="md" radius="sm"
            w={ { base: 480, xs: 550, md: 650 } }
        >
            <Flex gap="md">
                <Flex align="center">
                    <Image
                        src={ base64ToDataUri(displayedImage.image, displayedImage.type) } alt="Product Image"
                        fit="contain" h={ 170 } bg={ colorScheme === "dark" ? "dark.4" : "gray.2" }
                        miw={ { base: 110, xs: 150, md: 200 } } maw={ { base: 110, xs: 150, md: 200 } }
                        style={ isProductAvailable ? {
                            cursor: "pointer"
                        } : {
                            cursor: "pointer",
                            filter: "blur(2px) grayscale(50%) brightness(70%)",
                            opacity: 0.7
                        } }
                        onClick={ () => navigate(paths.app.productDetails.getHref(product.id.toString())) }
                    />
                </Flex>

                <Flex direction="column" maw="45%" justify="center">
                    <Anchor
                        size="xl" fw={ 700 } lineClamp={ 2 }
                        c="var(--mantine-color-text)"
                        component={ Link } to={ paths.app.productDetails.getHref(product.id.toString()) }
                    >
                        { product.name }
                    </Anchor>

                    <Text size="xs" c="dimmed" mb="xs">
                        Sold by { " " }
                        <CustomLink to={ paths.app.productsByUser.getHref(product.seller.id.toString()) }>
                            { product.seller.name }
                        </CustomLink>
                    </Text>

                    { !isProductAvailable ? (
                        <Text c="red.5" size="md">
                            This product is no longer available
                        </Text>
                    ) : product.availableQuantity > 3 ? (
                        <Text c="teal.6" size="md">
                            In Stock
                        </Text>
                    ) : (
                        <Text c="orange.6" size="md">
                            Only { product.availableQuantity } left in stock!
                        </Text>
                    ) }

                    { isProductAvailable &&
                        <Flex mt="xl">
                            <CartButtonGroup
                                cartItem={ cartItem } product={ product }
                                cartEnabled={ cartEnabled } size="sm"
                            />
                        </Flex>
                    }
                </Flex>

                <Flex direction="column" ms="auto" justify="space-between" align="flex-end">
                    <Text c="dimmed" size="xs">
                        Product #{ product.id }
                    </Text>

                    <DeleteWishlistItemActionIcon productId={ product.id.toString() }/>

                    <Text c="dimmed" size="xs">
                        Added { `${ addedAt.getDate() }/${ addedAt.getMonth() + 1 }/${ addedAt.getFullYear() }` }
                    </Text>
                </Flex>
            </Flex>
        </Paper>
    );
}