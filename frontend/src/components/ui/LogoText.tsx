import { Text } from "@mantine/core";

export function LogoText() {
    return (
        <Text
            size="lg" variant="gradient" fw={ 700 }
            gradient={ { from: "paleIndigo.8", to: "paleIndigo.4", deg: 150 } }
        >
            ReSellMart
        </Text>
    );
}