import { AppRouter } from "./AppRouter.tsx";
import { MantineProvider } from "@mantine/core";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { AuthProvider } from "./providers/AuthProvider.tsx";

import '@mantine/core/styles.css';
import '@mantine/notifications/styles.css';
import { Notifications } from "@mantine/notifications";

const queryClient = new QueryClient();

function App() {
  return (
      <MantineProvider> {/* TODO: Theme and dark mode */ }
          <Notifications/>
          <QueryClientProvider client={ queryClient }>
              <AuthProvider>
                  <AppRouter/>
              </AuthProvider>
          </QueryClientProvider>
      </MantineProvider>
  )
}

export default App
