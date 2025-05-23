import { useGetCart } from "@/api/cart/getCart.ts";
import { useGetCartTotal } from "@/api/cart/getCartTotal.ts";
import { CartItemsList } from "@/features/app/cart/components/CartItemsList.tsx";
import { CartTotalCard } from "@/features/app/cart/components/CartTotalCard.tsx";
import { useAuth } from "@/hooks/useAuth.ts";
import { Flex, Loader, Text } from "@mantine/core";

export function CartPage() {
    const { user } = useAuth();

    const getCartQuery = useGetCart({ userId: user!.id.toString() });
    const getCartTotalQuery = useGetCartTotal({ userId: user!.id.toString() });

    if (getCartQuery.isPending || getCartTotalQuery.isPending) {
        return (
            <Flex align="center" justify="center" h="100vh" w="100%">
                <Loader type="bars" size="md"/>
            </Flex>
        );
    }

    if (getCartQuery.isError) {
        console.log("Error fetching cart", getCartQuery.error);
        return (
            <Text c="red.5">
                There was an error when fetching your cart. Please refresh and try again.
            </Text>
        );
    }

    if (getCartTotalQuery.isError) {
        console.log("Error fetching cart total", getCartTotalQuery.error);
        return (
            <Text c="red.5">
                There was an error when calculating your cart total. Please refresh and try again.
            </Text>
        );
    }

    const cartItems = getCartQuery.data?.data;
    const cartTotal = getCartTotalQuery.data?.data;
    const totalItems =
        cartItems.reduce((acc, item) => acc + item.quantity, 0);
    const cartValid = cartItems.every((item) =>
        !item.product.isDeleted && (item.quantity <= item.product.availableQuantity)
    );
    const cartEmpty = cartItems.length === 0;

    return (
        <>
            <title>{ `Cart | ReSellMart` }</title>

            <Flex
                direction={ { base: "column", md: "row" } }
                justify={ { base: "flex-start", md: "center" } }
                align={ { base: "center", md: "flex-start" } }
                mt="lg" gap="md"
            >
                <CartItemsList
                    cartItems={ cartItems } cartTotal={ cartTotal }
                    totalItems={ totalItems } cartValid={ cartValid }
                    cartEmpty={ cartEmpty }
                />

                { !cartEmpty &&
                    <CartTotalCard cartTotal={ cartTotal } totalItems={ totalItems } cartValid={ cartValid }/>
                }
            </Flex>
        </>
    );
}