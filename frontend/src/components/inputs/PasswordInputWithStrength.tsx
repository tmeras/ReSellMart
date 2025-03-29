import { PasswordInput, PasswordInputProps, Popover, Progress } from "@mantine/core";
import { getStrength, PasswordRequirement, requirements } from "./PasswordRequirement.tsx";
import { useState } from "react";

type PasswordRequirementProps = PasswordInputProps & {
    value: string;
}

export const PasswordInputWithStrength = ({ value, ...props }: PasswordRequirementProps) => {
    const [popoverOpened, setPopoverOpened] = useState(false);

    const strength = getStrength(value);
    const color = strength === 100 ? 'teal' : strength > 50 ? 'yellow' : 'red';

    return (
        <Popover opened={ popoverOpened } position="bottom" width="target" transitionProps={ { transition: 'pop' } }>
            <Popover.Target>
                <div
                    onFocusCapture={ () => setPopoverOpened(true) }
                    onBlurCapture={ () => setPopoverOpened(false) }
                >
                    <PasswordInput { ...props } />
                </div>
            </Popover.Target>

            <Popover.Dropdown>
                <Progress color={ color } value={ strength } size={ 5 } mb="xs"/>
                { requirements.map((re, index) => (
                    <PasswordRequirement key={ index } label={ re.label } meets={ re.requirement.test(value) }/>
                )) }
            </Popover.Dropdown>
        </Popover>
    )
}