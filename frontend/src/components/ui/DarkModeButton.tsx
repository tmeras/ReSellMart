import { ActionIcon, Tooltip, useMantineColorScheme, useMantineTheme } from "@mantine/core";
import { IconMoonStars, IconSun } from "@tabler/icons-react";

export function DarkModeButton() {
    const { colorScheme, toggleColorScheme } = useMantineColorScheme();
    const theme = useMantineTheme();

    return (
        colorScheme === "dark" ? (
            <Tooltip label="Toggle Appearance">
                <ActionIcon
                    aria-label="Dark Mode" onClick={ () => toggleColorScheme() }
                    variant="subtle" ms="sm" size={ 25 }
                >
                    <IconMoonStars color={ theme.colors.blue[6] }/>
                </ActionIcon>
            </Tooltip>
        ) : (
            <Tooltip label="Toggle Appearance">
                <ActionIcon
                    aria-label="Light Mode" onClick={ () => toggleColorScheme() }
                    variant="subtle" ms="sm" size={ 25 }
                >
                    <IconSun color={ theme.colors.yellow[4] }/>
                </ActionIcon>
            </Tooltip>
        )
    );
}