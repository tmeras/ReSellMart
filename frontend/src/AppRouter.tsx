import { ProductsByCategoryPage } from "@/pages/app/products/ProductsByCategoryPage.tsx";
import { ProductsPage } from "@/pages/app/products/ProductsPage.tsx";
import { useMemo } from "react";
import { ErrorBoundary } from "react-error-boundary";
import { createBrowserRouter, RouterProvider } from "react-router";
import { MainErrorBoundary } from "./components/error/MainErrorBoundary.tsx";
import { NotFound } from "./components/error/NotFound.tsx";
import { AppLayout } from "./components/layouts/AppLayout.tsx";
import { AuthLayout } from "./components/layouts/AuthLayout.tsx";
import { ProtectedRoute } from "./components/ProtectedRoute.tsx";
import { paths } from "./config/paths.ts";
import { ActivationPage } from "./pages/auth/ActivationPage.tsx";
import { LoginPage } from "./pages/auth/LoginPage.tsx";
import { RegistrationPage } from "./pages/auth/RegistrationPage.tsx";

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
                    path: paths.auth.login.path,
                    element: <LoginPage/>
                },
                {
                    path: paths.auth.register.path,
                    element: <RegistrationPage/>
                },
                {
                    path: paths.auth.activation.path,
                    element: <ActivationPage/>
                }
            ]
        },
        {
            element: (
                <ErrorBoundary FallbackComponent={ MainErrorBoundary }>
                    <ProtectedRoute>
                        <AppLayout/>
                    </ProtectedRoute>
                </ErrorBoundary>
            ),
            children: [
                {
                    path: paths.app.products.path,
                    element: <ProductsPage/>
                },
                {
                    path: paths.app.productByCategory.path,
                    element: <ProductsByCategoryPage/>
                },

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

