export const paths = {
    auth: {
        login: {
            path: "/auth/login",
            getHref: () => "/auth/login"
        },
        register: {
            path: "/auth/register",
            getHref: () => "/auth/register"
        },
        activation: {
            path: "/auth/activation",
            getHref: () => "/activation"
        }
    },

    app: {
        products: {
            path: "/app/products",
            getHref: () => "/app/products"
        },
        productByCategory: {
            path: "/app/products/categories/:categoryId",
            getHref: (id: string) => `/app/products/categories/${ id }`
        },
        productsByUser: {
            path: "/app/products/users/:userId",
            getHref: (id: string) => `/app/products/users/${ id }`
        },
        productDetails: {
            path: "/app/products/:productId",
            getHref: (id: string) => `/app/products/${ id }`
        }
    },
} as const