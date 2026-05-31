# 💊 Pharmacy Web App

An online pharmacy web application built with Spring Boot, PostgreSQL, and VNPay payment integration. The system encompasses a full buying/selling flow, cart management, ordering, and JWT security authentication.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.x |
| Security | Spring Security, JWT (Access Token) + Refresh Token |
| Database | PostgreSQL, Spring Data JPA |
| Payment | VNPay Sandbox |
| Docs | Springdoc OpenAPI (Swagger UI) |

---

## Key Features

### Customer
- Secure Registration / Login using JWT (Includes Access Token & Refresh Token).
- Browse and search for medicines, manufacturers, and categories.
- Shopping Cart — add, update quantities, or remove products from the cart.
- Order and pay online via VNPay gateway.
- View order history, send order cancellation/return requests.

### Admin
- Manage categories, manufacturers, and medicines (products).
- Inventory Management — import stock, track inventory history (Inventory Log).
- Order Processing — approve orders, confirm delivery.
- Manage Users and Roles.
- Approve or reject cancellation/return requests from customers.

### Payment (VNPay) & System
- Generate secure payment URLs using HMAC-SHA512 encryption.
- Handle IPN callbacks from VNPay (to update payment statuses automatically server-to-server).
- Return URL to redirect users after transactions.
- Health Check API to monitor server status.

---

## Basic Order Workflow

```text
PENDING      → [User self-cancels]      → CANCELLED
CONFIRMED    → [User requests cancel]   → CANCEL_REQUESTED
               [Admin approves]         → CANCELLED
               [Admin rejects]          → CONFIRMED
SHIPPING     → [User requests return]   → RETURN_REQUESTED
               [Admin confirms return]  → RETURNED
```

---

## Installation & Local Setup

### Prerequisites
- Java 17+
- PostgreSQL
- Maven

### Step 1 — Database Setup

Create a database in PostgreSQL.

### Step 2 — Application Configuration

The application loads its configuration from Environment Variables. You need to set up the following variables:

```bash
# PostgreSQL Configuration
PGURL=jdbc:postgresql://localhost:5432/[Your_DB_Name]
PGUSER=[PostgreSQL_Username]
PGPASSWORD=[PostgreSQL_Password]

# JWT Configuration
JWT_SECRET=[Your_JWT_Secret_String]

# VNPay Configuration (configured in application.properties or via env vars)
# vnpay.tmn-code=[Your_TMN_Code]
# vnpay.hash-secret=[Your_Hash_Secret]
```

### Step 3 — Run the Application

Run the application using Maven:

```bash
mvn spring-boot:run
```

Once successfully started, access Swagger UI at: `http://localhost:8080/swagger-ui.html` (Or the port specified by the `${PORT}` variable).

---

## Directory Structure (Packages)

The entire business logic is clearly organized following the MVC and RESTful API standard:

```text
src/main/java/Pharmacy/
├── Config/          # Security, JWT, CORS, Swagger, VNPay configs
├── Controllers/     # REST API Endpoints for Client, Admin & Health check
├── Services/        # Business Logic (Auth, Order, Cart, Medicine...)
├── Repositories/    # Data Access Layer (Extends JpaRepository)
├── Entities/        # Database Tables (Users, Orders, Cart, Medicine, Inventory...)
├── DTO/             # Data Transfer Objects (Request/Response payload)
└── Exceptions/      # Global Exception Handler & Custom Exceptions
```

> **Note**: The project includes frontend/static directories (HTML/CSS/JS) located in `src/main/resources/static` for serving the UI, but the Core API runs completely independently.

---

## Unit Testing

The project includes unit tests (using JUnit and Mockito) for critical services:
- `AuthServiceTest`
- `OrderServiceTest`
- `PaymentServiceTest`

Run tests using:
```bash
mvn test
```
