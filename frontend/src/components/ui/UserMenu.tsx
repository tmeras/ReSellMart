import { UserButton } from "@/components/ui/UserButton.tsx";
import { paths } from "@/config/paths.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { api } from "@/lib/apiClient.ts";
import { base64ToDataUri } from "@/utils/fileUtils.ts";
import { Menu, useMantineTheme } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconHeart, IconHome2, IconLogout, IconSettings, IconX } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router";

type UserMenuProps = {
    closeNavBar: () => void;
}

export function UserMenu({ closeNavBar }: UserMenuProps) {
    const theme = useMantineTheme();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const { user } = useAuth();

    async function handleLogout() {
        try {
            await api.get("/api/auth/logout");
            queryClient.removeQueries();
            window.location.href = paths.auth.login.path;
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
                    image={ user!.profileImage ? base64ToDataUri(user!.profileImage) : null }
                />
            </Menu.Target>

            <Menu.Dropdown>
                <Menu.Item
                    leftSection={ <IconHeart size={ 16 } color={ theme.colors.red[4] }/> }
                    onClick={ () => {
                        closeNavBar();
                        navigate(paths.app.wishlist.getHref());
                    } }
                >
                    Wishlist
                </Menu.Item>

                <Menu.Item
                    leftSection={ <IconHome2 size={ 16 }/> }
                    onClick={ () => {
                        closeNavBar();
                        navigate(paths.app.addresses.getHref());
                    } }
                >
                    Addresses
                </Menu.Item>

                <Menu.Item
                    leftSection={ <IconSettings size={ 16 }/> }
                    onClick={ () => {
                        closeNavBar();
                        navigate(paths.app.updateUser.getHref());
                    } }
                >
                    Account Settings
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