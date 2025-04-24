import { FileInputProps, Pill } from "@mantine/core";

// Used to display the selected files in a more user-friendly way via Pill components
export const FileInputValueComponent: FileInputProps['valueComponent'] = ({ value }) => {
    if (value === null) {
        return null;
    }

    if (Array.isArray(value)) {
        return (
            <Pill.Group>
                { value.map((file, index) => (
                    <Pill key={ index }>{ file.name }</Pill>
                )) }
            </Pill.Group>
        );
    }

    return <Pill>{ value.name }</Pill>;
};