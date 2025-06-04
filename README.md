# ReSellMart
A full-stack E-commerce application for buying and selling second-hand goods. Developed using Spring Boot (Java), React (TypeScript), and MySQL. This project is for learning purposes.


## 📋 Features
### Products
- Browse products with pagination, filtering, and sorting functionality
- Add products to the shopping cart or wishlist
- View products by category and by seller
- List products for sale by specifying product details including name, category, price, condition, images etc.
- View listed products and update their details

### Orders
- Checkout products (from various sellers) in cart and place orders, with purchase and sale email notifications to buyer and sellers, respectively
- Pay for orders using Stripe (sandbox mode)
- View sales and mark sold products as shipped
- View purchases and mark bought products as delivered

### Users
- Update account details including name, profile picture, home country
- Toggle Multi-Factor Authentication (MFA)
- Manage addresses
- Reset password (TODO)

### Admin
- Enable/disable users and promote them to admins
- View all products and toggle their availability
- Add, update, and delete product categories
- Generate statistics on products sold, revenue, registered users, and orders placed


## 🛠️ Tools & Technologies Used
### Frontend
- React (TypeScript)
- TanStack (React) Query
- TanStack Table
- React Router
- Mantine

### Backend
- Spring Boot (Java)
- Spring MVC, Security, Data JPA
- Flyway (DB migrations)
- Stripe API
-  TODO: Swagger (API documentation)

### Database
- MySQL (Dockerised)


## 🔐 Security 
-  Access & refresh token flow with JSON Web Tokens (JWTs)
 - Role-Based Access Control (RBAC) to distinguish between user and admin responsibilities
- Account activation via email upon registration
- MFA using One-Time Passwords (OTPs)

## 🚀 Deployment (In progress)
- Deployed Spring Boot backend to an Amazon EC2 instance
- Deployed React frontend to an Amazon S3 bucket
- Custom domain configured with Amazon Route 53
- CloudFront used as CDN for improved performance and HTTPS support

## ⚙️ CI/CD (GitHub Actions)
- Pipeline to build and run unit tests on push to feature branch
- Pipelines to build, run unit and integration tests, and deploy on pull request and on push to main branch


## API Documentation (TODO)
- TODO with swagger


## Running Instructions
### Requirements
- Java 17
- Node.js 18+
- Docker
- Stripe API secret key (for payments) & Stripe CLI
- Free ports:
    -   5173 for the frontend
	-	8080 for the backend
	-	3306 for the MySQL database
    -   1080 for MailDev server

#### Environment Variables 
- STRIPE_SECRET_KEY = {YOUR_SECRET_KEY_HERE}

### Steps
1. **Listen for Stripe Events** (for payments processing)
```bash
stripe listen --forward-to localhost:8080/api/orders/stripe-webhook
```
Set **application.stripe.webhook-secret** in *backend/src/main/resources/application-dev.yml* to
the provided webhook signing secret

2. **Run Backend**
(from project root)
```bash
cd backend
docker compose up -d mysql-resellmart mail-dev
./mvnw spring-boot:run
```

3. **Run Frontend** (from project root)
```bash
cd frontend
npm install
npm run dev
```
4. App now running at http://localhost:5173/auth/login  

Login and pay with the following:

| email         | password    | card |
|--------------|--------------|--------------|
| john@gmail.com| pass123| 4242 4242 4242 4242 |

**NOTES**:
-  Product images in stripe checkout page will not be visible as webhook endpoint is served over HTTP (not HTTPS) during development
- Emails can be viewed at http://localhost:1080/#/

### Running Tests (from project root)
```bash
cd backend
docker compose up -d mail-dev
./mvnw clean verify
```
