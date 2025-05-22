import { FileInputValueComponent } from "@/components/form/FileInputValueComponent.tsx";
import { updateUserInputSchema, useUpdateUser } from "@/features/app/users/api/updateUser.ts";
import { uploadUserImageInputSchema, useUploadUserImage } from "@/features/app/users/api/uploadUserImage.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { ACCEPTED_IMAGE_TYPES, MAX_FILE_SIZE } from "@/utils/constants.ts";
import { base64ToFile } from "@/utils/fileUtils.ts";
import { Avatar, Button, FileInput, Flex, Image, Modal, Paper, Select, Switch, Text, TextInput } from "@mantine/core";
import { useForm, zodResolver } from "@mantine/form";
import { notifications } from "@mantine/notifications";
import { IconAlertCircle, IconArrowBack, IconPhoto, IconShieldLock, IconX } from "@tabler/icons-react";
import { Country } from "country-state-city";
import { useEffect, useMemo, useState } from "react";
import { z } from "zod";

export function UpdateUserForm() {
    const { user, setUser } = useAuth();
    const [image, setImage] = useState<File | null>(null);
    const [imagePreviewUrl, setImagePreviewUrl] = useState<string>("");
    const [qrImageUri, setQrImageUri] = useState<string>("");

    const countryOptions = useMemo(() => {
        const countries = Country.getAllCountries();
        return countries.map((country) => country.name);
    }, []);

    const updateUserMutation = useUpdateUser();
    const uploadUserImageMutation = useUploadUserImage();

    const formInputSchema = updateUserInputSchema.merge(uploadUserImageInputSchema);
    type FormInput = z.infer<typeof formInputSchema>;

    const form = useForm<FormInput>({
        mode: "uncontrolled",
        validate: zodResolver(formInputSchema),
        // Disable form until form is initialised with logged-in user's data
        enhanceGetInputProps: (payload) => {
            if (!payload.form.initialized) {
                return { disabled: true };
            }

            return {};
        }
    });

    form.watch("image", ({ value: selectedImage }) => {
        if (!form.isValid("image")) {
            setImage(null);
            return;
        }

        setImage(selectedImage);
    });

    // Initialise form with logged-in user's data
    useEffect(() => {
        if (!form.initialized) {
            const userImage = user!.profileImage ?
                base64ToFile(user!.profileImage, "user.png") : null;

            form.initialize({
                name: user!.name,
                homeCountry: user!.homeCountry,
                isMfaEnabled: user!.isMfaEnabled,
                image: userImage
            });
        }
    }, [user]);

    // Create URL objects to preview images when they change
    useEffect(() => {
        if (image) {
            const newUrl = URL.createObjectURL(image);
            setImagePreviewUrl(newUrl);
        } else {
            setImagePreviewUrl("");
        }
    }, [image]);

    // Cleanup previous URL object
    useEffect(() => {
        return () => {
            if (imagePreviewUrl) URL.revokeObjectURL(imagePreviewUrl);
        };
    }, [imagePreviewUrl]);

    async function handleSubmit(values: typeof form.values) {
        try {
            // Upload image and rest of form details separately
            const { image, ...rest } = values;

            let response = await updateUserMutation.mutateAsync({
                userId: user!.id.toString(),
                data: { ...rest }
            });

            // If MFA was enabled, show QR image
            if (response.data.qrImageUri) setQrImageUri(response.data.qrImageUri);

            response = await uploadUserImageMutation.mutateAsync({
                userId: user!.id.toString(),
                data: { image }
            });
            const userImage = response.data.profileImage ?
                base64ToFile(response.data.profileImage, "user.png") : null;
            setImage(userImage);

            setUser(response.data);
            notifications.show({
                title: "Account settings successfully updated", message: "",
                color: "teal", withBorder: true
            });
        } catch (error) {
            console.log("Error updating user", error);
            notifications.show({
                title: "Something went wrong", message: "Please try updating your account details again",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    return (
        <>
            <Modal
                size="md" radius="md"
                centered withCloseButton={ false }
                closeOnEscape={ false } closeOnClickOutside={ false }
                opened={ qrImageUri !== "" } onClose={ () => setQrImageUri("") }
            >
                <Flex direction="column" align="center" justify="center">
                    <Flex gap="xs" mt="sm" align="center" justify="center">
                        <IconAlertCircle color="red" size={ 25 }/>
                        <Text size="lg" c="red.8" fw="bold">
                            IMPORTANT
                        </Text>
                    </Flex>

                    <Text size="lg" ta="center" my="xs" mb="sm">
                        Please scan the following QR code using your preferred authenticator app
                        to be able to sign in using MFA
                    </Text>

                    <Image src={ qrImageUri ? qrImageUri : null } w={ 250 } h={ 250 }/>

                    <Button
                        leftSection={ <IconArrowBack/> }
                        variant="light" mt="xl"
                        onClick={ () => setQrImageUri("") }
                    >
                        Back to account settings
                    </Button>
                </Flex>
            </Modal>

            <Flex w="100%" justify="center" align="center" mt="lg">
                <Paper withBorder shadow="lg" p="xl" radius="md" w={ 350 }>
                    <Flex justify="center">
                        <Avatar
                            src={ imagePreviewUrl ? imagePreviewUrl : null } name={ user!.name }
                            color="initials" size={ 90 }
                        />
                    </Flex>

                    <form onSubmit={ form.onSubmit(handleSubmit) }>
                        <TextInput
                            mt="sm"
                            label="Name" required withAsterisk={ false }
                            key={ form.key("name") }
                            { ...form.getInputProps("name") }
                        />

                        <Select
                            mt="sm"
                            label="Home country" placeholder="Pick a country"
                            required withAsterisk={ false } searchable
                            data={ countryOptions }
                            key={ form.key("homeCountry") }
                            { ...form.getInputProps("homeCountry") }
                        />

                        <FileInput
                            mt="sm"
                            label="Profile Image" clearable
                            description={ `${ MAX_FILE_SIZE / (1024 * 1024) }MB max` }
                            leftSection={ <IconPhoto size={ 16 }/> }
                            accept={ ACCEPTED_IMAGE_TYPES.join(",") }
                            valueComponent={ FileInputValueComponent }
                            key={ form.key("image") }
                            { ...form.getInputProps("image") }
                        />

                        <Switch
                            mt="md" size="sm"
                            label={
                                <span style={ { display: 'flex', alignItems: 'center' } }>
                                        Enable MFA
                                        <IconShieldLock size={ 22 } style={ { marginLeft: 5 } }/>
                                    </span>
                            }
                            key={ form.key("isMfaEnabled") }
                            { ...form.getInputProps("isMfaEnabled", { type: 'checkbox' }) }
                        />

                        <Button fullWidth mt="xl" type="submit" loading={ form.submitting }>
                            Update settings
                        </Button>
                    </form>
                </Paper>
            </Flex>
        </>
    );
}