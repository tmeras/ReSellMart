import imgUrl from "@/assets/user.png";
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
        <UnstyledButton ref={ ref } w="100%" { ...others }>
            <Flex gap="xs" align="center">
                <Avatar src={ image ? image : imgUrl } radius="xl" size={ 36 }/>

                <Flex direction="column">
                    <Text size="sm" fw={ 500 }>
                        { name }
                    </Text>

                    <Text size="xs" c="dimmed">
                        { email }
                    </Text>
                </Flex>

                <IconChevronRight size={ 16 } style={ { marginLeft: "auto" } }/>
            </Flex>
        </UnstyledButton>
    )
);