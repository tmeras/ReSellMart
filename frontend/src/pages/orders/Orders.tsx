import { useQuery } from "@tanstack/react-query";
import { api } from "../../lib/api-client.ts";

import { useAuth } from "../../hooks/useAuth.ts";

export const Orders = () => {
    const { user } = useAuth()

    const { data, error, isError, isLoading } = useQuery({
        queryKey: ["orders"],
        queryFn: () => api.get(`api/users/${ user?.id }/orders`).then(res => res.data)
    });

    if (isLoading) return <>Loading...</>;

    if (isError) return <div>There was an error: { error.message }</div>

    if (!data)
        return <div>Data is invalid</div>;

    console.log("fetched orders", data)

    return (
        <>
            { data.content.map((order) => <div key={ order.id }>{ order.id }</div>) }
        </>
    );
}