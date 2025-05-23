import { Text } from "@mantine/core";
import { IconCheck, IconX } from "@tabler/icons-react";

export const requirements = [
    { requirement: /[0-9]/, label: 'Includes number' },
    { requirement: /[a-z]/, label: 'Includes lowercase letter' },
    { requirement: /[A-Z]/, label: 'Includes uppercase letter' },
    { requirement: /[$&+,:;=?@#|'<>.^*()%!-]/, label: 'Includes special symbol' },
    { requirement: /.{8,}/, label: 'Includes at least 8 characters' }
] as const;

export const getStrength = (password: string) => {
    let multiplier = 0

    requirements.forEach((re) => {
        if (!re.requirement.test(password)) {
            multiplier += 1;
        }
    });

    return Math.max(100 - (100 / (requirements.length)) * multiplier, 10);
}

type PasswordRequirementProps = {
    meets: boolean;
    label: string;
}

export function PasswordRequirement({ meets, label }: PasswordRequirementProps) {
    return (
        <Text
            c={ meets ? 'teal' : 'red' }
            style={ { display: 'flex', alignItems: 'center' } }
            mt={ 7 } size="sm"
        >
            { meets ? <IconCheck size={ 14 }/> : <IconX size={ 14 }/> }
            <span style={ { marginLeft: 10 } }>{ label }</span>
        </Text>
    )
}