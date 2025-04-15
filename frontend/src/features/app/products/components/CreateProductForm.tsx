import { useGetCategories } from "@/api/categories/getCategories.ts";
import { FileInputValueComponent } from "@/components/form/FileInputValueComponent.tsx";
import { ImagePreviewCarousel } from "@/components/ui/ImagePreviewCarousel.tsx";
import { createProductInputSchema, useCreateProduct } from "@/features/app/products/api/createProduct.ts";
import {
    uploadProductImagesInputSchema,
    useUploadProductImages
} from "@/features/app/products/api/uploadProductImages.ts";
import {
    ACCEPTED_IMAGE_TYPES,
    MAX_FILE_SIZE,
    MAX_IMAGE_COUNT,
    MAX_PRODUCT_QUANTITY,
    PRODUCT_CONDITION,
    ProductConditionKeys
} from "@/utils/constants.ts";
import {
    Button,
    FileInput,
    Flex,
    Loader,
    NativeSelect,
    NumberInput,
    Paper,
    Text,
    Textarea,
    TextInput
} from "@mantine/core";
import { FormErrors, useForm, zodResolver } from "@mantine/form";
import { notifications } from "@mantine/notifications";
import { IconPhoto, IconX } from "@tabler/icons-react";
import { ChangeEvent, useState } from "react";

export function CreateProductForm() {
    const [images, setImages] = useState<File[]>([]);
    const [mainImageValue, setMainImageValue] = useState("0");

    const getCategoriesQuery = useGetCategories();
    const createProductMutation = useCreateProduct();
    const uploadProductImagesMutation = useUploadProductImages();

    const formInputSchema = createProductInputSchema.merge(uploadProductImagesInputSchema);

    const form = useForm<{
        name: string;
        description: string;
        price: number;
        availableQuantity: number;
        productCondition: ProductConditionKeys;
        categoryId: string;
        images: File[];
    }>({
        mode: "uncontrolled",
        initialValues: {
            name: "",
            description: "",
            price: 0,
            availableQuantity: 0,
            productCondition: "NEW",
            categoryId: "1",
            images: []
        },
        validate: zodResolver(formInputSchema)
    });

    form.watch("images", ({ value: images }) => {
        if (!form.isValid("images")) {
            setImages([]);
            return;
        }

        setImages(images);
    });

    async function handleSubmit(values: typeof form.values) {
        try {
            // Submit images separately from remaining form
            const { images, ...formValues } = values;
            const response = await createProductMutation.mutateAsync({ data: formValues });

            const productId = response.data.id;
            await uploadProductImagesMutation.mutateAsync({
                productId: productId.toString(),
                data: {
                    images
                }
            });

            notifications.show({
                title: "Product listing successfully created", message: "You can manage it in the `My Products` page",
                color: "teal", withBorder: true
            });
            //TODO: Redirect to my products
        } catch (error) {
            console.log("Error creating product", error);
            notifications.show({
                title: "Something went wrong", message: "Please try submitting the product details again",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    function handleErrors(errors: FormErrors) {
        // Check if any error key starts with "images."
        const imageErrors = Object.keys(errors).filter((key) => key.startsWith("images."));
        if (imageErrors.length > 0) {
            // Manually assign a top-level error to "images"
            form.setFieldError("images", errors[imageErrors[0]]);
        }
    }

    function handleMainImageChange(event: ChangeEvent<HTMLSelectElement>) {
        const selectedValue = Number(event.target.value);

        // Main image should be first
        form.reorderListItem("images", { from: selectedValue, to: 0 });
        setImages(form.getValues().images);
        setMainImageValue("0");
    }

    if (getCategoriesQuery.isLoading) {
        return (
            <Flex w="100%" h="100vh" align="center" justify="center">
                return <Loader size="md"/>
            </Flex>
        );
    }

    const categories = getCategoriesQuery.data?.data;

    if (getCategoriesQuery.isError || !categories) {
        return (
            <Text c="red.5">
                There was an error when fetching the product categories. Please refresh and try again.
            </Text>
        );
    }

    const parentCategories = categories.filter((category) => category.parentId === undefined);
    const categoryOptions = parentCategories.map((parent) => {
        const children =
            categories.filter((category) => category.parentId === parent.id);
        const childrenOptions = children.map((child) => {
            return {
                label: child.name,
                value: child.id.toString()
            };
        });

        return {
            group: parent.name,
            items: [
                {
                    label: parent.name,
                    value: parent.id.toString()
                },
                ...childrenOptions
            ]
        };
    });

    const conditionOptions = Object.keys(PRODUCT_CONDITION).map((condition) => {
        return {
            label: PRODUCT_CONDITION[condition as keyof typeof PRODUCT_CONDITION],
            value: condition
        };
    });

    const mainImageOptions = images.map((image, index) => {
        return {
            label: image.name,
            value: index.toString()
        };
    });

    return (
        <Flex
            direction={ { base: "column", md: "row" } }
            align="center" justify="center"
            gap="xl" mt="md"
        >
            <Paper withBorder shadow="lg" p="xl" radius="md" miw={ 400 } maw={ 500 }>
                <form onSubmit={ form.onSubmit(handleSubmit, handleErrors) }>
                    <TextInput
                        label="Name" required withAsterisk={ false }
                        key={ form.key("name") }
                        { ...form.getInputProps("name") }
                    />

                    <Textarea
                        mt="sm"
                        label="Description" required withAsterisk={ false }
                        description="Please provide a detailed description of the product"
                        minRows={ 4 } maxRows={ 6 } autosize resize="vertical"
                        key={ form.key("description") }
                        { ...form.getInputProps("description") }
                    />

                    <NumberInput
                        mt="sm"
                        label="Price" required withAsterisk={ false }
                        min={ 1 } allowNegative={ false } prefix="£"
                        fixedDecimalScale decimalScale={ 2 }
                        key={ form.key("price") }
                        { ...form.getInputProps("price") }
                    />

                    <NumberInput
                        mt="sm"
                        label="Available Quantity" required withAsterisk={ false }
                        min={ 1 } max={ MAX_PRODUCT_QUANTITY }
                        allowNegative={ false } allowDecimal={ false }
                        key={ form.key("availableQuantity") }
                        { ...form.getInputProps("availableQuantity") }
                    />

                    <NativeSelect
                        mt="sm"
                        label="Condition" required withAsterisk={ false }
                        data={ conditionOptions }
                        key={ form.key("productCondition") }
                        { ...form.getInputProps("productCondition") }
                    />

                    <NativeSelect
                        mt="sm"
                        label="Category" required withAsterisk={ false }
                        data={ categoryOptions }
                        key={ form.key("categoryId") }
                        { ...form.getInputProps("categoryId") }
                    />

                    <FileInput
                        mt="sm"
                        label="Images"
                        description={ `Max ${ MAX_IMAGE_COUNT } images of ${ MAX_FILE_SIZE / (1024 * 1024) }MB each` }
                        leftSection={ <IconPhoto size={ 16 }/> } clearable
                        multiple accept={ ACCEPTED_IMAGE_TYPES.join(",") }
                        valueComponent={ FileInputValueComponent }
                        key={ form.key("images") }
                        { ...form.getInputProps("images") }
                    />

                    { form.isValid("images") && images.length > 1 &&
                        <NativeSelect
                            mt="sm"
                            label="Main Image"
                            description="Select the image that will be used as the front cover for the product"
                            data={ mainImageOptions }
                            value={ mainImageValue } onChange={ handleMainImageChange }
                        />
                    }

                    <Button fullWidth mt="xl" type="submit" loading={ form.submitting }>
                        Create product listing
                    </Button>
                </form>
            </Paper>

            <ImagePreviewCarousel images={ images }/>
        </Flex>
    );
}