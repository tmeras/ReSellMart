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
        return <div>Loading....</div>;

    if (!user)
        return <Navigate to={ `${ paths.auth.login.path }?redirectTo=${ location.pathname }` } replace/>;

    return children;
}