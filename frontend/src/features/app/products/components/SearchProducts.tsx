import { ActionIcon, TextInput, TextInputProps } from "@mantine/core";
import { IconArrowRight, IconSearch } from "@tabler/icons-react";
import { useState } from "react";

type SearchProductsProps = TextInputProps & {
    handleSearch: (search: string) => void;
}

export function SearchProducts({ handleSearch, ...props }: SearchProductsProps) {
    const [search, setSearch] = useState("");

    return (
        <form
            style={ {
                display: "flex",
                alignItems: "center",
                justifyContent: "center"
            } }
            onSubmit={ (e) => {
                e.preventDefault();
                handleSearch(search); // Only trigger search when user has clicked on search button or pressed enter
            } }
        >
            <TextInput
                radius="lg" size="md" placeholder="Search products..."
                leftSection={ <IconSearch size={ 18 } stroke={ 2 }/> }
                rightSection={
                    <ActionIcon size={ 32 } radius="lg" variant="outline" type="submit">
                        <IconArrowRight size={ 18 } stroke={ 1.5 }/>
                    </ActionIcon>
                }
                value={ search } onChange={ (v) => setSearch(v.target.value) }
                { ...props }
            />
        </form>
    );
}