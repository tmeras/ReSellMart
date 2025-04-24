import { Button, Flex, Text, Title } from '@mantine/core';
import { IconAlertTriangleFilled, IconMoodCry } from "@tabler/icons-react";

type MainErrorBoundaryProps = {
    error: Error;
}

export function MainErrorBoundary({ error }: MainErrorBoundaryProps) {
    console.log("MainErrorFallback caught", error)

    return (
        <Flex direction="column" align="center" justify="center" h="80vh">
            <IconAlertTriangleFilled color="red" size={ 90 }/>

            <Flex align="center" gap={ 10 }>
                <Title ta="center">An unexpected error occurred </Title>
                <IconMoodCry size={ 40 }/>
            </Flex>

            <Text c="dimmed" size="lg" ta="center" mt="sm">
                Please refresh the page and try again.
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