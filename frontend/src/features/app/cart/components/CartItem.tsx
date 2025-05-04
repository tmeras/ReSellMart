import { CustomLink } from "@/components/ui/link/CustomLink.tsx";
import { paths } from "@/config/paths.ts";
import { CartItemQuantityInput } from "@/features/app/cart/components/CartItemQuantityInput.tsx";
import { DeleteCartItemButton } from "@/features/app/cart/components/DeleteCartItemButton.tsx";
import { CartItemResponse } from "@/types/api.ts";
import { base64ToDataUri } from "@/utils/fileUtils.ts";
import { Anchor, Flex, Image, Text, useMantineColorScheme } from "@mantine/core";
import { Link, useNavigate } from "react-router";

export type CartItemProps = {
    cartItem: CartItemResponse;
}

export function CartItem({ cartItem }: CartItemProps) {
    const { colorScheme } = useMantineColorScheme();
    const navigate = useNavigate();

    const product = cartItem.product;
    const displayedImage = product.images[0];
    const isProductAvailable = !product.isDeleted && product.availableQuantity > 0;

    return (
        <Flex gap="md" p="sm">
            <Flex align="center">
                <Image
                    src={ base64ToDataUri(displayedImage.image, displayedImage.type) } alt="Product Image"
                    fit="contain" h={ 170 } bg={ colorScheme === "dark" ? "dark.4" : "gray.2" }
                    miw={ { base: 110, xs: 130, md: 200 } } maw={ { base: 110, xs: 130, md: 200 } }
                    style={ isProductAvailable ? {
                        cursor: "pointer"
                    } : {
                        cursor: "pointer",
                        filter: "blur(2px) grayscale(50%) brightness(70%)",
                        opacity: 0.7
                    } }
                    onClick={ () =>
                        navigate(paths.app.productDetails.getHref(product.id.toString()))
                    }
                />
            </Flex>

            <Flex direction="column" maw="45%" mih={ 180 }>
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
                    <Text c="red.5" size="sm">
                        This product is no longer available
                    </Text>
                ) : product.availableQuantity > 3 ? (
                    <Text c="teal.6" size="sm">
                        In Stock
                    </Text>
                ) : (
                    <Text c="orange.6" size="sm">
                        Only { product.availableQuantity } left in stock!
                    </Text>
                ) }

                <Flex gap="sm" align="center" mt="auto">
                    { isProductAvailable &&
                        <CartItemQuantityInput cartItem={ cartItem } mt="lg"/>
                    }

                    <DeleteCartItemButton cartItem={ cartItem } mt={ 40 }/>
                </Flex>
            </Flex>

            <Flex align="center" ms="auto">
                £ { cartItem.price }
            </Flex>
        </Flex>
    );
}