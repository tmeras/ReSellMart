import { Outlet, useNavigate, useSearchParams } from "react-router";
import { useEffect } from "react";
import { useAuth } from "../../hooks/useAuth.ts";

export const AuthLayout = () => {
    const { user } = useAuth()

    const [searchParams] = useSearchParams();
    const redirectTo = searchParams.get('redirectTo');
    const navigate = useNavigate();

    useEffect(() => {
        if (user)
            if (redirectTo)
                navigate(redirectTo, { replace: true });
            else
                navigate("/app/products", { replace: true });
    }, [user, navigate, redirectTo]);

    return <Outlet/>
};