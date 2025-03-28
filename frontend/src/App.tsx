import '@mantine/core/styles.css';
import '@mantine/notifications/styles.css';

import { AppRouter } from "./AppRouter.tsx";
import { MantineProvider } from "@mantine/core";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { AuthProvider } from "./providers/AuthProvider.tsx";
import { Notifications } from "@mantine/notifications";
import { ErrorBoundary } from "react-error-boundary";
import { MainErrorBoundary } from "./components/error/MainErrorBoundary.tsx";

const queryClient = new QueryClient();

function App() {
  return (
      <ErrorBoundary FallbackComponent={ MainErrorBoundary }>
          <MantineProvider> {/* TODO: Theme and dark mode */ }
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
