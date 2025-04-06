import { ActionIcon, TextInput, TextInputProps } from "@mantine/core";
import { IconArrowRight, IconSearch } from "@tabler/icons-react";

type SearchProductsProps = TextInputProps & {
    search: string;
    setSearch: (search: string) => void;
    handleSearch: (search: string) => void;
}

export function SearchProducts({ search, setSearch, handleSearch, ...props }: SearchProductsProps) {

    return (
        <form
            style={ {
                width: "100%",
                display: "flex",
                alignItems: "center",
                justifyContent: "center"

            } }
            onSubmit={ (e) => {
                e.preventDefault();
                handleSearch(search);
            }
            }
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