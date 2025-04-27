import { useMakeAddressMain } from "@/features/app/user/api/makeAddressMain.ts";
import { useAuth } from "@/hooks/useAuth.ts";
import { Button } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { IconX } from "@tabler/icons-react";

export type MainAddressButtonProps = {
    addressId: string;
};

export function MainAddressButton({ addressId }: MainAddressButtonProps) {
    const { user } = useAuth();

    const makeAddressMainMutation = useMakeAddressMain({ userId: user!.id.toString() });

    async function makeAddressMain() {
        try {
            await makeAddressMainMutation.mutateAsync({ addressId });
        } catch (error) {
            console.log("Error making address main", error);
            notifications.show({
                title: "Something went wrong", message: "Please try setting address as main again",
                color: "red", icon: <IconX/>, withBorder: true
            });
        }
    }

    return (
        <Button
            size="compact-sm" onClick={ makeAddressMain }
            loading={ makeAddressMainMutation.isPending }
        >
            Set as main
        </Button>
    );
}