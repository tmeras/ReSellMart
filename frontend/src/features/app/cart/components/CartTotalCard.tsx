import { paths } from "@/config/paths.ts";
import { Button, Paper, Text, Tooltip } from "@mantine/core";
import { useNavigate } from "react-router";

export type CartTotalCardProps = {
    cartTotal: number;
    totalItems: number;
    cartValid: boolean;
}

export function CartTotalCard({ cartTotal, totalItems, cartValid }: CartTotalCardProps) {
    const navigate = useNavigate();

    return (
        <Paper
            p="md" radius="sm" miw={ 300 } maw={ 350 }
            withBorder shadow="lg" h="fit-content"
            style={ { position: "sticky", top: 80 } }
        >
            <Text size="lg" fw={ 700 }>
                Total ({ totalItems } items): £ { cartTotal }
            </Text>

            { cartValid ? (
                <Button
                    fullWidth mt="sm"
                    onClick={ () => navigate(paths.app.checkout.getHref()) }
                >
                    Proceed to checkout
                </Button>
            ) : (
                <Tooltip
                    events={ { hover: true, focus: false, touch: true } }
                    position="bottom" multiline w={ 350 }
                    label="Cannot checkout. Your cart includes unavailable products or
                         the requested quantity of some products exceed available stock"
                >
                    <Button fullWidth mt="sm" disabled>
                        Proceed to checkout
                    </Button>
                </Tooltip>
            ) }
        </Paper>
    );
}