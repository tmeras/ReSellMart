import { useUpdateCartItem } from "@/api/cart/updateCartItem.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { CartItemResponse } from "@/types/api.ts";
import { NumberInput, NumberInputProps } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconX } from "@tabler/icons-react";
import { useEffect, useRef, useState } from "react";

export type CartItemQuantityInputProps = NumberInputProps & {
    cartItem: CartItemResponse;
}

export function CartItemQuantityInput({ cartItem, ...rest }: CartItemQuantityInputProps) {
    const { user } = useAuth();
    const [quantity, setQuantity] = useState(1);
    const latestQuantityRef = useRef(1);

    useEffect(() => {
        setQuantity(cartItem.quantity);
    }, [cartItem]);

    const updateCartItemMutation = useUpdateCartItem({ userId: user!.id.toString() });

    const product = cartItem.product;
    const error = (cartItem.quantity > product.availableQuantity) ?
        `Maximum product quantity is ${ product.availableQuantity }` : "";

    async function handleQuantityChange(value: number) {
        setQuantity(value);
        latestQuantityRef.current = value;
    }

    async function updateQuantity() {
        const latestQuantity = latestQuantityRef.current;
        console.log("quantity in update", latestQuantity);

        if (latestQuantity >= 1 && latestQuantity <= product.availableQuantity) {
            try {
                await updateCartItemMutation.mutateAsync({
                    data: {
                        productId: product.id.toString(),
                        userId: user!.id.toString(),
                        quantity: latestQuantity
                    }
                });
            } catch (error) {
                console.log("Error updating cart item", error);
                notifications.show({
                    title: "Something went wrong", message: "Please refresh and try updating product quantity again",
                    color: "red", icon: <IconX/>, withBorder: true
                });
            }
        }
    }

    return (
        <NumberInput
            mt="sm" w={ 100 } size="xs"
            description={ `Max ${ product.availableQuantity }` }
            placeholder={ `${ quantity }` }
            min={ 1 } max={ product.availableQuantity }
            allowNegative={ false } allowDecimal={ false } value={ quantity }
            onChange={ (value) => typeof value === "number" && handleQuantityChange(value) }
            onBlur={ updateQuantity }
            disabled={ updateCartItemMutation.isPending }
            error={ error }
            { ...rest }
        />
    );
}