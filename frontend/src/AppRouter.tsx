import { ScrollToTop } from "@/components/ScrollToTop.tsx";
import { AdminCategoriesPage } from "@/pages/admin/AdminCategoriesPage.tsx";
import { AdminDashboardPage } from "@/pages/admin/AdminDashboardPage.tsx";
import { AdminProductsPage } from "@/pages/admin/AdminProductsPage.tsx";
import { AdminUsersPage } from "@/pages/admin/AdminUsersPage.tsx";
import { CheckoutPage } from "@/pages/app/orders/CheckoutPage.tsx";
import { PurchasesPage } from "@/pages/app/orders/PurchasesPage.tsx";
import { SalesPage } from "@/pages/app/orders/SalesPage.tsx";
import { CreateProductPage } from "@/pages/app/products/CreateProductPage.tsx";
import { ProductDetailsPage } from "@/pages/app/products/ProductDetailsPage.tsx";
import { ProductsByCategoryPage } from "@/pages/app/products/ProductsByCategoryPage.tsx";
import { ProductsByUserPage } from "@/pages/app/products/ProductsByUserPage.tsx";
import { ProductsPage } from "@/pages/app/products/ProductsPage.tsx";
import { SellerProductsPage } from "@/pages/app/products/SellerProductsPage.tsx";
import { UpdateProductPage } from "@/pages/app/products/UpdateProductPage.tsx";
import { AddressesPage } from "@/pages/app/user/AddressesPage.tsx";
import { CartPage } from "@/pages/app/user/CartPage.tsx";
import { UpdateUserPage } from "@/pages/app/user/UpdateUserPage.tsx";
import { WishlistPage } from "@/pages/app/user/WishlistPage.tsx";
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
                    <ScrollToTop/>
                    <AuthLayout/>
                </ErrorBoundary>
            ),
            children: [
                // TODO: Home page with latest products
                {
                    path: "",
                    element: <LoginPage />
                },
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
                    <ScrollToTop/>
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
                {
                    path: paths.app.productsByUser.path,
                    element: <ProductsByUserPage/>
                },
                {
                    path: paths.app.productDetails.path,
                    element: <ProductDetailsPage/>
                },
                {
                    path: paths.app.createProduct.path,
                    element: <CreateProductPage/>
                },
                {
                    path: paths.app.updateProduct.path,
                    element: <UpdateProductPage/>
                },
                {
                    path: paths.app.sellerProducts.path,
                    element: <SellerProductsPage/>
                },
                {
                    path: paths.app.addresses.path,
                    element: <AddressesPage/>
                },
                {
                    path: paths.app.updateUser.path,
                    element: <UpdateUserPage/>
                },
                {
                    path: paths.app.wishlist.path,
                    element: <WishlistPage/>
                },
                {
                    path: paths.app.cart.path,
                    element: <CartPage/>
                },
                {
                    path: paths.app.checkout.path,
                    element: <CheckoutPage/>
                },
                {
                    path: paths.app.purchases.path,
                    element: <PurchasesPage/>
                },
                {
                    path: paths.app.sales.path,
                    element: <SalesPage/>
                },
                {
                    path: paths.admin.dashboard.path,
                    element: <AdminDashboardPage />
                },
                {
                    path: paths.admin.categories.path,
                    element: <AdminCategoriesPage/>
                },
                {
                    path: paths.admin.users.path,
                    element: <AdminUsersPage/>
                },
                {
                    path: paths.admin.products.path,
                    element: <AdminProductsPage />
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

