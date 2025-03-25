import {Navigate, Outlet, useLocation} from "react-router";
import {paths} from "../config/paths.ts";
import {ReactNode} from "react";
import {useAuth} from "../hooks/useAuth.ts";

type ProtectedRouteProps = {
    children: ReactNode;
}

export const ProtectedRoute = () => {
    const {user, isLoadingUser} = useAuth();

    const location = useLocation();

    if (isLoadingUser)
        return <div>Loading....</div>

    if (!user)
        return <Navigate to={`${paths.auth.login}?redirectTo=${location.pathname}`} replace/>

    return <Outlet/>;
}