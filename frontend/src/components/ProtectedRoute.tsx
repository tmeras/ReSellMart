import { Flex, Loader } from "@mantine/core";
import { ReactNode } from "react";
import { Navigate, useLocation } from "react-router";
import { paths } from "../config/paths.ts";
import { useAuth } from "../hooks/useAuth.ts";

type ProtectedRouteProps = {
    children: ReactNode;
}

export function ProtectedRoute({ children }: ProtectedRouteProps) {
    const {user, isLoadingUser} = useAuth();
    const location = useLocation();

    if (isLoadingUser)
        return (
            <Flex align="center" justify="center" h="100vh">
                <Loader type="bars" size="md"/>
            </Flex>
        );

    if (!user)
        return <Navigate to={ `${ paths.auth.login.path }?redirectTo=${ location.pathname }` } replace/>;

    return children;
}