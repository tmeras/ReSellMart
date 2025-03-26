import { createBrowserRouter, RouterProvider } from "react-router";
import { LoginPage } from "./pages/auth/LoginPage.tsx";
import { Products } from "./pages/products.tsx";
import { Orders } from "./pages/orders/Orders.tsx";
import { ProtectedRoute } from "./components/ProtectedRoute.tsx";
import { paths } from "./config/paths.ts";
import { AuthLayout } from "./components/layouts/AuthLayout.tsx";
import { useMemo } from "react";
import { RegisterPage } from "./pages/auth/RegistrationPage.tsx";

const createAppRouter = () =>
    createBrowserRouter([
        {
            element: <AuthLayout/>,
            children: [
                {
                    path: paths.auth.login,
                    element: <LoginPage/>
                },
                {
                    path: paths.auth.register,
                    element: <RegisterPage/>
                }
            ]
        },
        {
            element: (
                <ProtectedRoute/>
            ),
            // TODO: Add error boundary
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
        } // TODO: Not found path
    ]);

export const AppRouter = () => {
    const router = useMemo(() => createAppRouter(), []);

    return <RouterProvider router={ router }/>
}

