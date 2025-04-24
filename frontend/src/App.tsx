import '@mantine/core/styles.css';
import '@mantine/notifications/styles.css';
import '@mantine/carousel/styles.css';
import { createTheme, MantineProvider } from "@mantine/core";
import { Notifications } from "@mantine/notifications";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ErrorBoundary } from "react-error-boundary";


import { AppRouter } from "./AppRouter.tsx";
import { MainErrorBoundary } from "./components/error/MainErrorBoundary.tsx";
import { AuthProvider } from "./providers/AuthProvider.tsx";

const queryClient = new QueryClient();

const theme = createTheme({
    primaryColor: "paleIndigo",
    colors: {
        paleIndigo: [
            "#eff2ff",
            "#dfe2f2",
            "#bdc2de",
            "#99a0ca",
            "#7a84b9",
            "#6672af",
            "#5c69ac",
            "#4c5897",
            "#424e88",
            "#36437a"
        ]
    }
});

function App() {
  return (
      <ErrorBoundary FallbackComponent={ MainErrorBoundary }>
          <MantineProvider theme={ theme } defaultColorScheme="light">
              <Notifications/>
              <QueryClientProvider client={ queryClient }>
                  <AuthProvider>
                      <AppRouter/>
                  </AuthProvider>
              </QueryClientProvider>
          </MantineProvider>
      </ErrorBoundary>
  )
}

export default App
