import { ReactNode, RefAttributes } from "react";
import { Link, LinkProps } from "react-router";
import classes from "./CustomLink.module.css";

export type CustomLinkProps = LinkProps & RefAttributes<HTMLAnchorElement> & {
    children: ReactNode;
};

export function CustomLink({ children, ...props }: CustomLinkProps) {
    return (
        <Link
            className={ classes.customLink }
            { ...props }
        >
            { children }
        </Link>
    );
}