# Enterprise Invoice Management System

A RESTful enterprise invoice management application built with Java 21 and Spring Boot. The system provides secure authentication, supplier management, invoice processing, approval workflows, audit logging, and financial dashboards.

## 🚀 Key Features

### Authentication & Security
- JWT-based authentication.
- Role-based access control (RBAC).
- BCrypt password hashing.
- Stateless session management.
- Protected REST API endpoints.

### Supplier Management
- Create, retrieve, update, and delete suppliers.
- Supplier information management.
- Supplier status validation.

### Invoice Management
- Create and manage invoices.
- Automatic invoice number generation.
- Invoice line-item management.
- Automatic subtotal, tax, and total calculations.
- Invoice status tracking.
- Draft invoice update and deletion.

### Approval Workflow
- Submit invoices for approval.
- Multi-approver workflow.
- Approve or reject invoice submissions.
- Track individual approval decisions.
- Automatic invoice status updates.

### Audit Logging
- Record important system activities.
- Track user actions.
- Retrieve audit logs by entity or user.
- Restrict audit log access to administrators.

### Financial Dashboard
- Invoice summary statistics.
- Invoice status breakdown.
- Outstanding invoice reports.
- Approval statistics.
- Monthly spending reports.
- Supplier reports.

## 🛠️ Technology Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Backend Framework | Spring Boot 4.1.2 |
| Build Tool | Apache Maven |
| Database | MySQL 8.0 |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security |
| Authentication | JWT (JJWT) |
| API Documentation | Swagger / OpenAPI |
| Testing | JUnit 5, Mockito, Spring Boot Test |
| Containerization | Docker |
| Orchestration | Docker Compose |

## 🏗️ Architecture

The application follows a layered architecture:

```text
Client / Swagger UI
        |
        v
REST Controllers
        |
        v
Service Layer
        |
        v
Repository Layer
        |
        v
MySQL Database
```

Security is handled through Spring Security and a JWT authentication filter.

## 👥 User Roles

| Role | Responsibilities |
|---|---|
| ADMIN | Full system administration and audit log access |
| FINANCE | Manage suppliers and invoices, submit invoices for approval |
| APPROVER | Review, approve, or reject assigned invoices |

## 📡 API Endpoints

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/login` | Authenticate user and generate JWT |

### Suppliers

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/suppliers` | Retrieve all suppliers |
| GET | `/api/suppliers/{id}` | Retrieve supplier details |
| POST | `/api/suppliers` | Create supplier |
| PUT | `/api/suppliers/{id}` | Update supplier |
| DELETE | `/api/suppliers/{id}` | Delete supplier |

### Invoices

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/invoices` | Retrieve invoices |
| GET | `/api/invoices/{id}` | Retrieve invoice details |
| POST | `/api/invoices` | Create invoice |
| PUT | `/api/invoices/{id}` | Update draft invoice |
| DELETE | `/api/invoices/{id}` | Delete draft invoice |

### Approval Workflow

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/invoices/{id}/submit` | Submit invoice |
| POST | `/api/invoices/{id}/approve` | Approve invoice |
| POST | `/api/invoices/{id}/reject` | Reject invoice |
| GET | `/api/invoices/{id}/approvals` | Retrieve approval history |

### Dashboard

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/dashboard/summary` | Dashboard summary |
| GET | `/api/dashboard/status-summary` | Invoice status statistics |
| GET | `/api/dashboard/outstanding-invoices` | Outstanding invoices |
| GET | `/api/dashboard/approval-statistics` | Approval statistics |
| GET | `/api/dashboard/monthly-spending` | Monthly spending |
| GET | `/api/dashboard/supplier-report` | Supplier report |

### Audit Logs

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/audit-logs` | Retrieve audit logs |
| GET | `/api/audit-logs/entity/{entityType}/{entityId}` | Retrieve logs by entity |
| GET | `/api/audit-logs/user/{userId}` | Retrieve logs by user |

## ⚙️ Getting Started

### Prerequisites

Install the following:

- Java 21 (for local development)
- Docker Desktop
- Git

Docker Compose is used to run the application and MySQL database.

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/invoice-management.git
cd invoice-management
```

### 2. Configure Environment Variables

Create a `.env` file in the project root directory.

```dotenv
MYSQL_ROOT_PASSWORD=your_secure_root_password
DB_NAME=invoice_management
DB_USERNAME=invoice_app
DB_PASSWORD=your_secure_app_password
JWT_SECRET=your_base64_encoded_32_byte_secret
```

Generate a secure JWT secret. For example, using PowerShell:

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

Use the generated value as `JWT_SECRET`.

**Important:** Never commit `.env` or expose database credentials and JWT secrets.

### 3. Start the Application

```bash
docker compose up --build -d
```

### 4. Verify Running Containers

```bash
docker compose ps
```

View application logs:

```bash
docker compose logs -f app
```

### 5. Access the Application

| Resource | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| REST API | http://localhost:8080/api |

### 6. Stop the Application

```bash
docker compose down
```

To remove containers and the Docker database volume:

```bash
docker compose down -v
```

**Warning:** The second command permanently removes the Docker database volume and its stored data.

## 🧪 Testing

The project includes unit tests and controller integration tests.

Latest verified test execution:

```text
Tests run: 71
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

Run tests locally:

```bash
mvn clean test
```

## 🔐 Security Considerations

- Passwords are hashed using BCrypt.
- JWT protects authenticated API endpoints.
- Role-based authorization restricts sensitive operations.
- Audit logs provide activity traceability.
- Environment variables are used for sensitive configuration.
- Docker Compose separates application and database services.

## 🐳 Docker Services

| Service | Container | Port |
|---|---|---|
| Spring Boot Application | `invoice-management-app` | 8080 |
| MySQL Database | `invoice-mysql` | 3308 |

The application connects to MySQL through the internal Docker network using `mysql:3306`.

## 📌 Project Status

Core backend development, automated testing, Dockerization, and Swagger API verification have been completed.

Planned improvements:

- GitHub Actions CI/CD pipeline.
- Production deployment.
- Enhanced API monitoring.
- Additional integration and security testing.

## 👨‍💻 Author

**Muhamad Nazeem Al Ghozali**

Java Backend Developer | Software Engineer

Technologies: Java, Spring Boot, REST API, MySQL, Docker, and Spring Security.
