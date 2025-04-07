import { ActionIcon, Tooltip, useMantineColorScheme } from "@mantine/core";
import { IconBrightnessDown, IconMoon } from "@tabler/icons-react";

export function DarkModeButton() {
    const { colorScheme, toggleColorScheme } = useMantineColorScheme();

    return (
        colorScheme === "dark" ? (
            <Tooltip label="Light Mode">
                <ActionIcon
                    aria-label="Light Mode" onClick={ () => toggleColorScheme() }
                    variant="filled" ms="sm" size="md" mt={ 2 }
                >
                    <IconBrightnessDown size={ 60 }/>
                </ActionIcon>
            </Tooltip>
        ) : (
            <Tooltip label="Dark Mode">
                <ActionIcon
                    aria-label="Dark Mode" onClick={ () => toggleColorScheme() }
                    variant="outline" ms="sm" size="md" mt={ 2 }
                >
                    <IconMoon size={ 50 }/>
                </ActionIcon>
            </Tooltip>
        )
    );
}