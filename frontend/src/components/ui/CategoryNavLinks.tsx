import { useGetCategories } from "@/api/categories/getCategories.ts";
import { paths } from "@/config/paths.ts";
import { Flex, Loader, NavLink, Tooltip } from "@mantine/core";
import { IconBrandAppleArcade, IconCpu2, IconHome, IconList, IconShirt } from "@tabler/icons-react";
import { NavLink as RouterNavLink } from "react-router";

// Icons to be displayed in the navbar for the main (parent) categories
const categoryIconMap = new Map([
    ["Electronics", <IconCpu2 size={ 18 }/>],
    ["Clothing", <IconShirt size={ 18 }/>],
    ["Home & Kitchen", <IconHome size={ 18 }/>],
    ["Entertainment", <IconBrandAppleArcade size={ 18 }/>]
]);

type CategoryNavLinksProps = {
    closeNavBar: () => void;
}

export function CategoryNavLinks({ closeNavBar }: CategoryNavLinksProps) {
    const getCategoriesQuery = useGetCategories();

    if (getCategoriesQuery.isPending) {
        return (
            <Flex align="center" justify="center" mt="xl">
                <Loader size="sm"/>
            </Flex>
        );
    }

    if (getCategoriesQuery.isError) {
        console.log("Get categories error", getCategoriesQuery.error);
        return (
            <Tooltip
                w={ 250 } multiline
                label="Could not fetch product categories. Please try refreshing the page."
                events={ { hover: true, focus: false, touch: true } }
            >
                <span>
                    <NavLink
                        label="Product Categories"
                        leftSection={ <IconList size={ 18 }/> }
                        disabled
                    />
                </span>
            </Tooltip>
        );
    }

    const categories = getCategoriesQuery.data?.data;

    const parentCategories =
        categories.filter(category => category.parentId === undefined);

    const navLinks = parentCategories.map(parent => {
        const icon = categoryIconMap.get(parent.name);

        const childrenCategories =
            categories.filter(category => category.parentId === parent.id);

        const childrenNavLinks = childrenCategories.map(child =>
            <NavLink
                key={ child.id } label={ child.name }
                component={ RouterNavLink } onClick={ closeNavBar }
                to={ paths.app.productByCategory.getHref(child.id.toString()) }
            />
        );

        return (
            <NavLink
                key={ parent.id } label={ parent.name }
                leftSection={ icon } childrenOffset={ 35 }
            >
                <NavLink
                    label="All"
                    component={ RouterNavLink } onClick={ closeNavBar }
                    to={ paths.app.productByCategory.getHref(parent.id.toString()) }
                />

                { childrenNavLinks }
            </NavLink>
        );
    });

    return (
        <NavLink
            label="Product Categories"
            leftSection={ <IconList size={ 18 }/> }
            defaultOpened
        >
            { navLinks }
        </NavLink>
    );
}