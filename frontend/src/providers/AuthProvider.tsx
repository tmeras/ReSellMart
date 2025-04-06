import { api } from "@/lib/apiClient.ts";
import { UserResponse } from "@/types/api.tsx";
import { InternalAxiosRequestConfig } from "axios";
import { createContext, ReactNode, useEffect, useLayoutEffect, useMemo, useState } from "react";


type AuthContext = {
    accessToken: string | null;
    user: UserResponse | null;
    isLoadingUser: boolean;
    setAccessToken: (token: string) => void;
    setUser: (user: UserResponse | null) => void;
    setIsLoadingUser: (isLoading: boolean) => void;
};

export const AuthContext = createContext<AuthContext | null>(null);

type AuthProviderProps = {
    children: ReactNode;
}

export const AuthProvider = ({children}: AuthProviderProps) => {
    const [accessToken, setAccessToken] = useState<string | null>(null);
    const [user, setUser] = useState<UserResponse | null>(null);
    const [isLoadingUser, setIsLoadingUser] = useState<boolean>(false);

    type AxiosRequestConfig = InternalAxiosRequestConfig & { _retry?: boolean };

    useLayoutEffect(() => {
        const responseInterceptor = api.interceptors.response.use(
            (response) => {
                return response;
            },
            async (error) => {
                const originalRequest = error.config;

                // Invalid or missing access token in header; attempt refresh
                if (error.response.status === 401 && !originalRequest._retry &&
                    (error.response.data.error === "No access token in Bearer header")
                    || (error.response.data.error === "Invalid access token")
                ) {
                    try {
                        const response = await api.post("api/auth/refresh", {},)
                        console.log("Refresh success", response);
                        setAccessToken(response.data.accessToken)

                        originalRequest._retry = true;

                        // Retry request with new access token
                        originalRequest.headers.Authorization = "Bearer " + response.data.accessToken;
                        return api(originalRequest)
                    } catch (error) {
                        console.log("Couldn't refresh", error)
                        setAccessToken(null);
                        setUser(null);
                    }
                }

                return Promise.reject(error);
            }
        );

        return () => {
            api.interceptors.response.eject(responseInterceptor)
        }
    }, []);

    useLayoutEffect(() => {
        console.log("Setting accessToken in header to", accessToken)

        const requestInterceptor = api.interceptors.request.use((config: AxiosRequestConfig) => {
            config.headers.Authorization =
                !config._retry && accessToken
                    ? `Bearer ${accessToken}`
                    : config.headers.Authorization;

            return config;
        })

        if (accessToken) {
            setIsLoadingUser(true);
            fetchMe().then(() => setIsLoadingUser(false));
        }

        return () => {
            api.interceptors.request.eject(requestInterceptor)
        }
    }, [accessToken]);

    useEffect(() => {
        setIsLoadingUser(true);
        fetchMe().then(() => setIsLoadingUser(false));
    }, []);

    const fetchMe = async () => {
        try {
            const response = await api.get("api/users/me");
            console.log("Got user", response.data)
            setUser(response.data);
        } catch (error) {
            console.log("Error fetching user", error);
            setUser(null);
        }
    };

    const contextValue = useMemo(
        () => ({
            accessToken,
            user,
            setAccessToken,
            setUser,
            isLoadingUser,
            setIsLoadingUser
        }), [accessToken, user, isLoadingUser]
    )

    return (
        <AuthContext.Provider value={contextValue}>
            {children}
        </AuthContext.Provider>
    )
}

