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
        },
        createProduct: {
            path: "/app/products/create",
            getHref: () => "/app/products/create"
        },
        updateProduct: {
            path: "/app/products/:productId/update",
            getHref: (id: string) => `/app/products/${ id }/update`
        },
        sellerProducts: {
            path: "/app/my-products",
            getHref: () => "/app/my-products"
        },
        addresses: {
            path: "/app/my-addresses",
            getHref: () => "/app/my-addresses"
        },
        updateUser: {
            path: "/app/account-settings",
            getHref: () => "/app/account-settings"
        },
        wishlist: {
            path: "/app/wishlist",
            getHref: () => "/app/wishlist"
        },
        cart: {
            path: "/app/cart",
            getHref: () => "/app/cart"
        },
        checkout: {
            path: "/app/checkout",
            getHref: () => "/app/checkout"
        },
        purchases: {
            path: "/app/my-purchases",
            getHref: () => "/app/my-purchases"
        },
        sales: {
            path: "/app/my-sales",
            getHref: () => "/app/my-sales"
        }
    },
    admin: {
        categories: {
            path: "/admin/categories",
            getHref: () => "/admin/categories"
        },
        users: {
            path: "/admin/users",
            getHref: () => "/admin/users"
        },
        products: {
            path: "/admin/products",
            getHref: () => "/admin/products"
        }
    }
} as const