import { createBrowserRouter, RouterProvider } from "react-router";
import { LoginPage } from "./pages/auth/LoginPage.tsx";
import { Products } from "./pages/products.tsx";
import { Orders } from "./pages/orders/Orders.tsx";
import { ProtectedRoute } from "./components/ProtectedRoute.tsx";
import { paths } from "./config/paths.ts";
import { AuthLayout } from "./components/layouts/AuthLayout.tsx";
import { useMemo } from "react";
import { RegistrationPage } from "./pages/auth/RegistrationPage.tsx";
import { ActivationPage } from "./pages/auth/ActivationPage.tsx";
import { MainErrorBoundary } from "./components/error/MainErrorBoundary.tsx";
import { ErrorBoundary } from "react-error-boundary";
import { NotFound } from "./components/error/NotFound.tsx";

const createAppRouter = () =>
    createBrowserRouter([
        {
            element: (
                <ErrorBoundary FallbackComponent={ MainErrorBoundary }>
                    <AuthLayout/>
                </ErrorBoundary>
            ),
            children: [
                {
                    path: paths.auth.login,
                    element: <LoginPage/>
                },
                {
                    path: paths.auth.register,
                    element: <RegistrationPage/>
                },
                {
                    path: paths.auth.activation,
                    element: <ActivationPage/>
                }
            ]
        },
        {
            element: (
                <ProtectedRoute/>
                // TODO: Add main error boundary at app root too
            ),
            children: [
                {
                    path: paths.app.products,
                    element: <Products/>
                },
                {
                    path: paths.app.orders,
                    element: <Orders/>
                }
            ]
        },
        {
            path: "*",
            element: <NotFound/>
        }
    ]);

export const AppRouter = () => {
    const router = useMemo(() => createAppRouter(), []);

    return <RouterProvider router={ router }/>
}

