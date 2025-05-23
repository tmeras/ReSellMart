import { Avatar, Flex, Text, UnstyledButton } from "@mantine/core";
import { IconChevronRight } from "@tabler/icons-react";
import { ComponentPropsWithoutRef, forwardRef } from "react";

type UserButtonProps = ComponentPropsWithoutRef<"button"> & {
    name: string,
    email: string,
    image: string | null
}

export const UserButton = forwardRef<HTMLButtonElement, UserButtonProps>(
    ({ name, email, image, ...others }: UserButtonProps, ref) => (
        <UnstyledButton ref={ ref } w="fit-content" { ...others }>
            <Flex gap="xs" align="center">
                <Avatar
                    src={ image ? image : null }
                    radius="xl" size={ 36 }
                    name={ name } color="initials"
                />

                <Flex direction="column">
                    <Text size="sm" fw={ 500 }>
                        { name }
                    </Text>

                    <Text size="xs" c="dimmed">
                        { email }
                    </Text>
                </Flex>

                <IconChevronRight size={ 16 } style={ { marginLeft: "15px" } }/>
            </Flex>
        </UnstyledButton>
    )
);