import { paths } from "@/config/paths.ts";
import { api } from "@/lib/apiClient.ts";
import { ProductResponse } from "@/types/api.ts";
import { PRODUCT_CONDITION } from "@/utils/constants.ts";
import { base64ToDataUri } from "@/utils/fileUtils.ts";
import {
    Button,
    Card,
    Flex,
    Grid,
    Group,
    Image,
    List,
    Loader,
    Text,
    Title,
    useMantineColorScheme
} from "@mantine/core";
import { IconChecklist, IconLeaf, IconList, IconPackages, IconPaywall, IconTool } from "@tabler/icons-react";
import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router";
import imgUrl from "@/assets/home.png";

export function HomePage() {
    const {colorScheme} = useMantineColorScheme();
    const [latestProducts, setLatestProducts] = useState<ProductResponse[]>([]);
    const [isError, setIsError] = useState(false);


    useEffect( () => {
        async function fetchLatestProducts() {
            try {
                const response = await api.get<ProductResponse[]>("/api/products/latest");
                setLatestProducts(response.data);
            } catch (error) {
                console.error("Error fetching latest products:", error);
                setIsError(true);
            }
        }

        fetchLatestProducts();
    }, []);

    const productCards = useMemo(() =>
        latestProducts.map((product: ProductResponse) => {
            // Find the product image that should be displayed as front cover
            const displayedImage = product.images[0];

            return (
                <Grid.Col span={ { base: 12, sm: 6, md: 4, lg: 3 } } key={ product.id }>
                    <Flex justify="center">
                        <Card w={ 300 } withBorder>
                            <Card.Section withBorder>
                                <Image
                                    src={ base64ToDataUri(displayedImage.image, displayedImage.type) } alt="Product Image"
                                    fit="contain" h={ 150 }
                                    bg={ colorScheme === "dark" ? "dark.4" : "gray.2" }
                                />
                            </Card.Section>

                            <Card.Section withBorder inheritPadding mt="xs">
                                <Text size="xl" lineClamp={2} fw={500} h={70}>
                                    { product.name }
                                </Text>
                            </Card.Section>

                            <Card.Section inheritPadding withBorder mt="sm">
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
                                                { PRODUCT_CONDITION[product.condition] }
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

                            <Card.Section withBorder inheritPadding mt={10} mb={1}>
                                <Text span size="xl" fw={ 500 }>
                                    £ { product.price.toFixed(2) }
                                </Text>

                                { product.previousPrice && product.previousPrice > product.price &&
                                    <Text span size="md" fw={ 300 } ms="xs" td="line-through">
                                        { product.previousPrice.toFixed(2) }
                                    </Text>
                                }
                            </Card.Section>
                        </Card>
                    </Flex>
                </Grid.Col>
            );
        })
    , [colorScheme, latestProducts]);

    return (
        <>
            <title>{`Welcome | ReSellMart`}</title>

            <Flex mih="75vh" align="center" justify="center">
                <Flex gap="xl">
                    <Flex direction="column" maw={480}>
                        <Title fz={{base: 33, md: 44}} >
                            Buy Smart.
                            <br/> Sell Sustainably
                        </Title>

                        <Text c="dimmed" mt="md">
                            Discover great deals on second-hand goods, or give your old products a new home.
                            Simple, secure, and eco-friendly.
                        </Text>

                        <List mt={30} spacing="sm" size="sm">
                            <List.Item icon={<IconChecklist />}>
                                <b>Fast & Easy Listings</b> – Post your items in minutes with a smooth, guided listing process
                            </List.Item>

                            <List.Item icon={<IconPaywall />}>
                                <b>Secure Payments</b> – Buy and sell with confidence through secure Stripe payments
                            </List.Item>

                            <List.Item icon={<IconPackages />}>
                                <b>15+ Product Categories</b> – From electronics to home goods, buy or sell across a wide
                                range of product types
                            </List.Item>

                            <List.Item icon={<IconLeaf />}>
                                <b>Eco-Friendly Impact</b> – Every item resold helps cut landfill waste and reduce CO₂ emissions
                            </List.Item>
                        </List>

                        <Group mt={30}>
                            <Button
                                radius="xl" size="md"
                                component={Link} to={paths.auth.register.path}
                            >
                                Register
                            </Button>

                            <Button
                                variant="light" radius="xl" size="md"
                                component={Link} to={paths.auth.login.path}
                            >
                                Sign in
                            </Button>
                        </Group>
                    </Flex>

                    <Image
                        src={imgUrl} h={400} w={400}
                        radius="sm"
                        display={{base: "none", md: "block"}}
                    />
                </Flex>
            </Flex>

            <Title order={2} mt="sm" mb="xl" ta="center" fz="30">
                Latest Products
            </Title>

            {isError ? (
                <Text c="red">
                    There was an error fetching the latest products. Please refresh and try again.
                </Text>
            ) : productCards.length == 0 ? (
                <Flex justify="center">
                    <Loader type="bars" />
                </Flex>
            ) : (
                <Grid gutter="lg" mb="lg">
                    {productCards}
                </Grid>
            )}
        </>
    );
}