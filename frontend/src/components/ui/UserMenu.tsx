import { UserButton } from "@/components/ui/UserButton.tsx";
import { paths } from "@/config/paths.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { api } from "@/lib/api-client.ts";
import { byteToBase64 } from "@/utils/fileUtils.ts";
import { Menu, useMantineTheme } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconHeart, IconLogout, IconSettings, IconX } from "@tabler/icons-react";
import { useNavigate } from "react-router";

export function UserMenu() {
    const theme = useMantineTheme();
    const navigate = useNavigate();
    const { user, setUser } = useAuth();

    async function handleLogout() {
        try {
            await api.get("/api/auth/logout");
            setUser(null);
            navigate(paths.auth.login);
        } catch (error) {
            console.log("Logout error:", error);

            notifications.show({
                title: "Something went wrong", message: "Please retry logout",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    return (
        <Menu
            withArrow position="top"
            transitionProps={ { transition: "pop" } }
        >
            <Menu.Target>
                <UserButton
                    name={ user!.name } email={ user!.email }
                    image={ user!.profileImage ? byteToBase64(user!.profileImage) : null }
                />
            </Menu.Target>

            <Menu.Dropdown>
                <Menu.Item leftSection={ <IconSettings size={ 16 }/> }>
                    Account Settings
                </Menu.Item>

                <Menu.Item leftSection={ <IconHeart size={ 16 } color={ theme.colors.red[6] }/> }>
                    Wishlist
                </Menu.Item>

                <Menu.Item
                    color="red" leftSection={ <IconLogout size={ 16 }/> }
                    onClick={ handleLogout }
                >
                    Logout
                </Menu.Item>
            </Menu.Dropdown>
        </Menu>
    );
}