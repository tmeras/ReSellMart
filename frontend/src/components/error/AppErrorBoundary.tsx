import { Button, Flex, Text, Title } from "@mantine/core";
import { IconAlertTriangleFilled, IconMoodCry } from "@tabler/icons-react";

type AppErrorBoundaryProps = {
    error: Error
};

export function AppErrorBoundary({ error }: AppErrorBoundaryProps) {
    console.log("AppErrorFallback caught", error);

    return (
        <Flex direction="column" align="center" justify="center" h="80vh">
            <IconAlertTriangleFilled color="red" size={ 90 }/>

            <Flex align="center" gap={ 10 }>
                <Title ta="center">An unexpected error occurred </Title>
                <IconMoodCry size={ 40 }/>
            </Flex>

            <Text c="dimmed" size="lg" ta="center" mt="sm">
                Please use the navigation bar or refresh the page and try again.
            </Text>

            <Button
                variant="light" size="md" mt="md"
                onClick={ () => window.location.reload() }
            >
                Refresh now
            </Button>
        </Flex>
    );
}