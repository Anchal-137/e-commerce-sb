# 🛒 E-Commerce Backend API

A robust, production-grade E-Commerce REST API built with **Spring Boot** and **MongoDB**. This backend provides a comprehensive solution for online shopping platforms, featuring secure authentication, product management, order processing, and analytics.

---

## 🚀 Key Features

*   **🔒 Security & Authentication**
    *   JWT-based Stateless Authentication
    *   Role-Based Access Control (RBAC): `ADMIN`, `SELLER`, `USER`
    *   Secure Password Hashing with BCrypt

*   **📦 Product Management**
    *   CRUD operations for Products
    *   Advanced Search & Filtering (Category, Price Range, Keywords)
    *   Pagination & Sorting support
    *   Inventory Management

*   **🛒 Shopping Cart & Orders**
    *   Full Cart functionality (Add, Update, Remove, Clear)
    *   Order Placement & History
    *   Order Status Tracking (Pending -> Confirmed -> Shipped -> Delivered)

*   **💳 Payments**
    *   Integrated Payment Intents (Simulation of Stripe Integration)
    *   Secure Payment Processing Flow

*   **📧 Notifications**
    *   Async Email Notifications (Welcome, Order Confirmation, Shipping Updates)

*   **📊 Analytics (Admin Dashboard)**
    *   Sales Reports (Daily/Monthly)
    *   Top Selling Products
    *   User Growth Statistics

*   **🛠️ Technical Highlights**
    *   **Caching**: Caffeine Cache for high-performance reads
    *   **Rate Limiting**: Bucket4j for API protection
    *   **Documentation**: Integrated Swagger UI / OpenAPI 3.0
    *   **Docker**: Dockerfile & Compose support for easy deployment

---

## 🛠️ Tech Stack

*   **Framework**: Spring Boot 3.1.2
*   **Language**: Java 17
*   **Database**: MongoDB
*   **Build Tool**: Gradle
*   **Security**: Spring Security 6, JJWT
*   **Documentation**: SpringDoc OpenAPI (Swagger)

---

## ⚙️ Getting Started

### Prerequisites

*   Java 17+ installed
*   MongoDB installed (or use the provided Docker Compose)

### 🏃‍♂️ Run Locally via Docker (Recommended)

```bash
docker-compose up --build
```

The API will be available at `http://localhost:8080`.

### 🏃‍♂️ Run Locally via Gradle

1.  **Clone the repository**
    ```bash
    git clone https://github.com/Anchal-137/e-commerce-sb.git
    cd e-commerce-sb
    ```

2.  **Configure Environment**
    *   The app uses default settings in `src/main/resources/application.yml`.
    *   For production, update the `MongoDB URI`, `JWT Secret`, and `Mail Credentials` in environment variables.

3.  **Build & Run**
    ```bash
    ./gradlew bootRun
    ```

---

## 📚 API Documentation

Once the application is running, access the full interactive API documentation at:

👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

### Quick Endpoint Reference

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| **Auth** | | | |
| `POST` | `/api/auth/register` | Register a new user | ❌ |
| `POST` | `/api/auth/login` | Login & get JWT token | ❌ |
| **Products** | | | |
| `GET` | `/api/products` | Get paginated products | ❌ |
| `POST` | `/api/products` | Create product | ✅ (Admin/Seller) |
| **Cart** | | | |
| `GET` | `/api/cart` | Get user cart | ✅ |
| `POST` | `/api/cart/items` | Add item to cart | ✅ |
| **Orders** | | | |
| `POST` | `/api/orders` | Place an order | ✅ |
| `GET` | `/api/orders` | Get my orders | ✅ |

---

## 🧪 How to Test

1.  **Register a User**: Use `/api/auth/register` to create an account.
2.  **Login**: Use `/api/auth/login` to get an `accessToken`.
3.  **Authorize**: In Swagger/Postman, use the token as a Bearer Token: `Bearer eyJhbGci...`
4.  **Explore**: You can now access potential secured endpoints like placing orders!

---

## 📂 Project Structure

```
src/main/java/com/example/ecommerce
├── config/       # Security, Swagger, Cache configs
├── controller/   # REST Controllers
├── dto/          # Data Transfer Objects
├── exception/    # Global Exception Handling
├── filter/       # JWT & Rate Limiting Filters
├── model/        # MongoDB Documents
├── repository/   # data Access Layer
├── security/     # Auth Logic (UserDetailService)
└── service/      # Business Logic
```

---

## 📝 License

This project is licensed under the MIT License.
