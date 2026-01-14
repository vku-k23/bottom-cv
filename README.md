# BottomCV - Job Search Platform

![Architecture Diagram](assets/diagram-export-4-23-2025-8_37_58-AM.png)

**BottomCV** is a comprehensive job search and recruitment platform that connects job seekers with employers. The platform provides advanced features including AI-powered job search assistance, payment processing, real-time notifications, and a robust candidate management system.

## 📋 Table of Contents

- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [System Architecture](#-system-architecture)
- [Prerequisites](#-prerequisites)
- [Installation & Setup](#-installation--setup)
- [Configuration](#-configuration)
- [API Documentation](#-api-documentation)
- [Deployment](#-deployment)
- [CI/CD Pipeline](#-cicd-pipeline)
- [Security](#-security)
- [Monitoring & Logging](#-monitoring--logging)
- [Development](#-development)
- [Contributing](#-contributing)
- [Support & Contact](#-support--contact)

## ✨ Features

### Core Functionality
- **User Authentication & Authorization**: JWT-based authentication with role-based access control (CANDIDATE, EMPLOYER, ADMIN)
- **Job Management**: Create, search, filter, and manage job postings with advanced search capabilities
- **CV Management**: Upload, store, and manage candidate resumes
- **Application System**: Submit and track job applications with status management
- **Company Profiles**: Company registration, verification, and profile management

### Advanced Features
- **AI-Powered Chatbot**: Google Gemini integration for intelligent job search assistance
- **Payment Processing**: Stripe integration for subscription and payment handling
- **Email Notifications**: Automated email service using Gmail SMTP
- **Job Alerts**: Real-time job alert notifications for candidates
- **Interview Management**: Schedule and manage interview processes
- **Saved Jobs & Candidates**: Bookmark functionality for both job seekers and employers
- **Review System**: Company and candidate review capabilities
- **Reporting System**: Abuse reporting and moderation tools
- **Blog System**: Content management for job-related articles
- **Advanced Search**: Apache Lucene integration for full-text search capabilities
- **Admin Dashboard**: Comprehensive admin panel for system management

## 🛠 Technology Stack

### Backend
- **Framework**: Spring Boot 3.3.3
- **Language**: Java 17
- **Security**: Spring Security with JWT authentication
- **Database**: PostgreSQL 16
- **ORM**: Spring Data JPA / Hibernate
- **Caching**: Redis with Jedis client
- **File Storage**: MinIO (S3-compatible object storage)
- **Search Engine**: Apache Lucene 9.11.1
- **Message Queue**: RabbitMQ (Spring AMQP)
- **Email**: Spring Mail (Gmail SMTP)
- **Payment**: Stripe Java SDK 24.16.0
- **AI Integration**: Google Gemini API (REST)

### DevOps & Infrastructure
- **Containerization**: Docker & Docker Compose
- **CI/CD**: Jenkins
- **Code Quality**: SonarQube
- **API Documentation**: SpringDoc OpenAPI 3 (Swagger UI)
- **Monitoring**: Prometheus & Grafana (mentioned in architecture)
- **Version Control**: Git / GitHub

### Frontend
- **Framework**: ReactJS (separate repository: `bottom-cv-fe`)

## 🏗 System Architecture

### Component Overview
```
┌─────────────┐
│   ReactJS   │  Frontend Application
│  Frontend   │
└──────┬──────┘
       │ REST API
       ▼
┌─────────────────┐
│  Spring Boot    │  Backend API Server
│    Backend      │  (Port: 8088)
└─────┬───────┬───┘
      │       │
      ▼       ▼
┌─────────┐ ┌─────────┐
│PostgreSQL│ │  Redis │  Data Storage & Caching
│Database │ │  Cache │
└─────────┘ └─────────┘
      │
      ▼
┌─────────┐
│  MinIO  │  Object Storage (Files, Images, CVs)
└─────────┘
```

### Data Flow
1. User interacts with React frontend
2. Frontend sends HTTP requests to Spring Boot backend via REST APIs
3. Backend processes requests, validates authentication/authorization
4. Backend interacts with PostgreSQL for data persistence
5. Redis is used for caching and session management
6. MinIO handles file storage operations (images, resumes, documents)
7. External services: Stripe (payments), Gmail (email), Gemini (AI chat)
8. Responses are sent back to frontend for user display

### Deployment Architecture
- **Containerized Services**: All services run in Docker containers
- **Orchestration**: Docker Compose for local development
- **Production**: Deployed on AWS EC2 instance
- **Network**: Services communicate via Docker bridge network (`bottom-cv-network`)

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK)**: Version 17 or higher
- **Maven**: Version 3.8.3 or higher
- **Docker**: Version 20.10 or higher
- **Docker Compose**: Version 2.0 or higher
- **Git**: For version control
- **PostgreSQL Client**: Optional, for direct database access
- **Redis Client**: Optional, for cache inspection

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/vku-k23/bottom-cv.git
cd bottom-cv
```

### 2. Environment Variables

Create a `.env` file in the project root with the following variables:

```env
# Email Configuration
GMAIL_USERNAME=your-email@gmail.com
GMAIL_PASSWORD=your-app-password

# Stripe Configuration
STRIPE_SECRET_KEY=sk_test_...
STRIPE_PUBLISHABLE_KEY=pk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...

# Google Gemini API
GEMINI_API_KEY=AIzaSy...
```

> **Note**: For Gmail, you'll need to generate an [App Password](https://support.google.com/accounts/answer/185833) if 2FA is enabled.

### 3. Start Services with Docker Compose

```bash
# Start all services (PostgreSQL, Redis, MinIO, Backend, Frontend)
docker-compose up -d

# Or start specific services
docker-compose up -d postgres redis minio bottom-cv

# View logs
docker-compose logs -f bottom-cv
```

### 4. Verify Services

- **Backend API**: http://localhost:8088
- **Swagger UI**: http://localhost:8088/swagger-ui.html
- **API Docs**: http://localhost:8088/v3/api-docs
- **MinIO Console**: http://localhost:9001 (minioadmin/minioadmin)
- **PgAdmin**: http://localhost:5050 (admin@admin.com/admin)
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379

### 5. Local Development (Without Docker)

#### Start Dependencies
```bash
# Start PostgreSQL, Redis, and MinIO only
docker-compose up -d postgres redis minio
```

#### Build and Run Backend
```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

The application will start on `http://localhost:8088`

## ⚙️ Configuration

### Application Configuration

Main configuration is in `src/main/resources/application.yml`:

```yaml
server:
  port: 8088

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bottom_cv
    username: postgres
    password: postgres
  
  data:
    redis:
      host: localhost
      port: 6379
      password: 123456

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: bottom-cv-storage
```

### Database Setup

The application uses Liquibase for database migrations. Migrations are located in `src/main/resources/db/`.

On first startup, the database schema will be automatically created.

### MinIO Bucket Setup

1. Access MinIO Console at http://localhost:9001
2. Login with credentials: `minioadmin` / `minioadmin`
3. Create bucket: `bottom-cv-storage`
4. Set bucket policy to allow public read access if needed

## 📚 API Documentation

### Swagger UI

Interactive API documentation is available at:
- **Swagger UI**: http://localhost:8088/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8088/v3/api-docs

### API Endpoints Overview

#### Authentication (`/api/v1/auth`)
- `POST /api/v1/auth/signup` - User registration
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/refresh-token` - Refresh JWT token

#### Jobs (`/api/v1`)
- `GET /api/v1/front/jobs` - Search and list jobs (public)
- `POST /api/v1/back/jobs` - Create job (EMPLOYER, ADMIN)
- `GET /api/v1/back/jobs` - List jobs for dashboard
- `PUT /api/v1/back/jobs/{id}` - Update job
- `DELETE /api/v1/back/jobs/{id}` - Delete job

#### Applications (`/api/v1`)
- `POST /api/v1/front/applies` - Submit application (CANDIDATE)
- `GET /api/v1/back/applies` - List applications (EMPLOYER, ADMIN)

#### AI Chat (`/api/v1`)
- `POST /api/v1/chat` - Send message to AI chatbot

#### Payments (`/api/v1`)
- `POST /api/v1/back/payments/create-session` - Create Stripe checkout session
- `POST /api/v1/public/payments/webhook` - Stripe webhook handler

#### Files (`/api/v1`)
- `POST /api/v1/files/upload` - Upload file to MinIO
- `GET /api/v1/files/{id}` - Download file

> See Swagger UI for complete API documentation with request/response schemas.

### Authentication

Most endpoints require JWT authentication. Include the token in the Authorization header:

```bash
Authorization: Bearer <your-jwt-token>
```

## 🚢 Deployment

### Docker Deployment

#### Build Docker Image

```bash
docker build -t bottom-cv:latest .
```

#### Run Container

```bash
docker run -d \
  --name bottom-cv \
  -p 8088:8088 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/bottom_cv \
  -e GMAIL_USERNAME=${GMAIL_USERNAME} \
  -e GMAIL_PASSWORD=${GMAIL_PASSWORD} \
  -e STRIPE_SECRET_KEY=${STRIPE_SECRET_KEY} \
  -e GEMINI_API_KEY=${GEMINI_API_KEY} \
  bottom-cv:latest
```

### Production Deployment (AWS EC2)

The application is configured for deployment on AWS EC2 using Docker Compose:

1. **Provision EC2 Instance**
   - Recommended: t3.medium or larger
   - Security groups: Allow ports 22, 80, 443, 8088

2. **Install Dependencies**
   ```bash
   sudo apt-get update
   sudo apt-get install -y docker.io docker-compose git
   ```

3. **Clone and Deploy**
   ```bash
   git clone https://github.com/vku-k23/bottom-cv.git
   cd bottom-cv
   # Configure .env file
   docker-compose up -d
   ```

4. **Set Up Reverse Proxy** (Nginx recommended)
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;
       
       location / {
           proxy_pass http://localhost:8088;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }
   }
   ```

## 🔄 CI/CD Pipeline

### Jenkins Pipeline

The project includes a `Jenkinsfile` for automated CI/CD:

**Pipeline Stages:**
1. **Deploy**: 
   - Pulls latest code from GitHub
   - Builds Docker images
   - Stops existing containers
   - Deploys new containers

**Configuration:**
- **Repository**: https://github.com/vku-k23/bottom-cv.git
- **Branch**: master
- **Deployment Directory**: `/home/ubuntu/bottom-cv`

**Jenkins Setup:**
1. Create new Pipeline job in Jenkins
2. Configure to use `Jenkinsfile` from SCM
3. Set up credentials for GitHub access
4. Configure environment variables in Jenkins

### Automated Deployment

The pipeline automatically:
- Builds Docker images on code push
- Runs tests (if configured)
- Deploys to EC2 instance
- Updates running containers

## 🔒 Security

### Security Features

- **JWT Authentication**: Secure token-based authentication
- **Role-Based Access Control (RBAC)**: CANDIDATE, EMPLOYER, ADMIN roles
- **Password Encryption**: BCrypt password hashing
- **HTTPS Support**: Configured for production (reverse proxy)
- **CORS Configuration**: Configured for frontend domain
- **Input Validation**: Jakarta Validation on all endpoints
- **SQL Injection Protection**: JPA/Hibernate parameterized queries
- **Secret Management**: Environment variables for sensitive data

### Security Best Practices

1. **Never commit secrets** to version control
2. **Use strong JWT secrets** in production
3. **Enable HTTPS** in production
4. **Regular security updates** for dependencies
5. **Database credentials** should be rotated regularly
6. **MinIO access keys** should be changed from defaults

### Environment Variables Security

All sensitive configuration should be set via environment variables:
- Database credentials
- JWT secret keys
- API keys (Stripe, Gemini, Gmail)
- MinIO credentials

## 📊 Monitoring & Logging

### Application Logging

Logging is configured in `application.yml`:
- **Log Level**: DEBUG for development, INFO/WARN for production
- **Log Files**: Check `app.log` in project root

### Health Checks

Spring Boot Actuator endpoints:
- **Health**: http://localhost:8088/actuator/health
- **Info**: http://localhost:8088/actuator/info
- **Metrics**: http://localhost:8088/actuator/metrics

### Monitoring Stack

- **Prometheus**: Metrics collection
- **Grafana**: Metrics visualization and dashboards
- **SonarQube**: Code quality analysis (port 9002)

Access SonarQube at http://localhost:9002 (if running via docker-compose)

## 💻 Development

### Project Structure

```
bottom-cv/
├── src/
│   ├── main/
│   │   ├── java/com/cnpm/bottomcv/
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── service/        # Business logic
│   │   │   ├── repository/     # Data access layer
│   │   │   ├── dto/            # Data transfer objects
│   │   │   ├── entity/         # JPA entities
│   │   │   ├── config/         # Configuration classes
│   │   │   └── security/       # Security configuration
│   │   └── resources/
│   │       ├── application.yml # Application configuration
│   │       ├── db/             # Liquibase migrations
│   │       └── templates/      # Email templates
│   └── test/                   # Unit and integration tests
├── docker-compose.yml          # Docker services configuration
├── Dockerfile                  # Backend Docker image
├── Jenkinsfile                 # CI/CD pipeline
├── pom.xml                     # Maven dependencies
└── README.md                   # This file
```

### Running Tests

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

### Code Quality

```bash
# Run SonarQube analysis
docker-compose up -d sonarqube
# Access at http://localhost:9002
```

### Database Migrations

Liquibase migrations are in `src/main/resources/db/`. Migrations run automatically on application startup.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

- Follow Java naming conventions
- Use Lombok for boilerplate code reduction
- Write unit tests for new features
- Update API documentation (Swagger annotations)
- Follow RESTful API design principles

## 📞 Support & Contact

### Project Information

- **Application Name**: BottomCV
- **Version**: 1.0.0
- **Description**: Bottom CV, the application to find job for everyone

### Contact Information

- **Developer**: Nguyen Quoc Viet
- **Email**: vietnq.23ceb@vku.udn.vn
- **Support Email**: support@bottomcv.com
- **Repository**: https://github.com/vku-k23/bottom-cv

### Support Channels

- **On-Call Support**: (+84)123456789
- **Brand**: BottomCV
- **Logo**: https://i.postimg.cc/4xcnS1yG/bottomcv.png

## 📄 License

This project is licensed under the Apache 2.0 License.

## 🙏 Acknowledgments

- Spring Boot community
- All contributors and maintainers
- Open source libraries and tools used in this project

---

**Built with ❤️ by the BottomCV Team**
