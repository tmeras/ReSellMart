import { Carousel } from "@mantine/carousel";
import { Flex, Image, Paper, Text, useMantineColorScheme } from "@mantine/core";
import { useEffect, useState } from "react";

type ImagePreviewCarouselProps = {
    images: File[];
};

export function ImagePreviewCarousel({ images }: ImagePreviewCarouselProps) {
    const { colorScheme } = useMantineColorScheme();
    const [imagePreviewUrls, setImagePreviewUrls] = useState<string[]>([]);

    useEffect(() => {
        // Create new URLs
        const newUrls = images.map((image) => URL.createObjectURL(image));
        setImagePreviewUrls(newUrls);
    }, [images]);

    // Cleanup previous URL objects
    useEffect(() => {
        return () => {
            imagePreviewUrls.forEach((url) => URL.revokeObjectURL(url));
        };
    }, [imagePreviewUrls]);

    return (
        <Paper withBorder shadow="lg" p={ 30 } radius="md" miw={ 350 } maw={ 650 }>
            { imagePreviewUrls.length > 0 ? (
                <Carousel
                    h={ { base: 350, md: 550 } } w={ { base: 350, md: 550 } }
                    slideGap="xl" align="center"
                    withIndicators={ imagePreviewUrls.length > 1 }
                    withControls={ imagePreviewUrls.length > 1 }
                    loop
                >
                    { imagePreviewUrls.map((url, index) => (
                        <Flex key={ index } gap="sm">
                            <Carousel.Slide>
                                <Image
                                    src={ url } alt={ `Preview image ${ index + 1 }` }
                                    bg={ colorScheme === "dark" ? "dark.4" : "gray.2" }
                                    fit="contain" radius="md"
                                    h={ { base: 330, md: 550 } } w={ { base: 330, md: 550 } }
                                />
                            </Carousel.Slide>
                        </Flex>
                    )) }
                </Carousel>
            ) : (
                <Flex
                    h={ { base: 350, md: 550 } } w={ { base: 350, md: 550 } }
                    align="center" justify="center"
                    bg={ colorScheme === "dark" ? "dark.4" : "gray.2" }
                >
                    <Text size="lg" fw={ 600 }>
                        Selected images will be previewed here
                    </Text>
                </Flex>
            ) }
        </Paper>
    );

}