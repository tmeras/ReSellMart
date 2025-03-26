import { useQuery } from "@tanstack/react-query";
import { api } from "../lib/api-client.ts";
import { Link } from "react-router";

export const Products = () => {

    const { data, error, isError, isLoading } = useQuery({
        queryKey: ["products"],
        queryFn: () => api.get("api/products/others").then(res => res.data)
    });

    if (isLoading) return <>Loading...</>;

    if (isError) return <div>There was an error: { error.message }</div>

    if (!data)
        return <div>Data is invalid</div>;

    console.log("fetched products", data)

    return (
        <>
            { data.content.map((product) => <div key={ product.id }>{ product.name }</div>) }

            <Link to="/app/orders"> GOOO</Link>
        </>
    );
}