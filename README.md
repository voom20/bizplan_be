# AI Co-Pilot for First-time Founders (Bizplan Backend)

## 📖 Project Overview

**Bizplan** is an intelligent partner designed to transform the complex business planning process into a data-driven decision-making journey. It helps first-time founders and small business owners quickly create high-quality business plans to pass funding gates (government grants, loans) and focus on sustainable growth.

### 🎯 Vision
> "Transform the complex business planning process into a data-driven decision-making journey that reduces failure rates."

## ✨ Key Features

- **🚀 Submission Wizard**
  - Step-by-step guide tailored for government and bank forms
  - Auto-save and progress tracking
  - 100% template compatibility

- **💰 Financial Auto-Engine**
  - 3-year P&L and cash flow projections from key variables
  - Unit Economics (LTV, CAC, BEP) calculations
  - Deterministic rule-based engine (No hallucinations)

- **🤖 AI Drafting (Co-Pilot)**
  - Context-aware writing assistance using **Google Gemini** & **LangChain**
  - 'Easy' and 'Expert' modes for different user needs
  - Section-by-section regeneration

- **📊 PMF Diagnostic**
  - Product-Market Fit analysis based on standard frameworks
  - Risk identification and recommendations

- **📄 Document Export**
  - PDF export with professional formatting
  - HTML preview support
  - (HWP support planned for future release)

## 🛠 Technical Stack

### Backend Core (API Server)
| Component | Technology |
|-----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.x |
| Build | Gradle |
| API | REST (OpenAPI 3.0 + SpringDoc) |
| Database | MySQL 8.x / H2 (dev) |
| Migration | Flyway |
| Security | Spring Security, AES-256, BCrypt |
| Testing | JUnit 5, Mockito |

### AI Engine (Microservice)
| Component | Technology |
|-----------|------------|
| Language | Python 3.10+ |
| Framework | FastAPI |
| AI | LangChain + Google Gemini |
| Testing | Pytest |
| Repository | [bizplan_ai](https://github.com/voom20/bizplan_ai) |

### Monitoring & DevOps
| Component | Technology |
|-----------|------------|
| Metrics | Micrometer + Prometheus |
| Visualization | Grafana |
| Load Testing | k6 |
| Container | Docker, Docker Compose |

## 🚀 Getting Started

### Prerequisites
- Java 21 (JDK)
- Gradle 8.x
- Docker & Docker Compose (optional)
- MySQL 8.0+ (or use H2 for development)

### Quick Start

```bash
# Clone the repository
git clone https://github.com/voom20/bizplan_be.git
cd bizplan_be

# Build the project
./gradlew build

# Run the application (H2 in-memory DB)
./gradlew bootRun

# Access the application
# API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
# H2 Console: http://localhost:8080/h2-console
```

### With MySQL (Production)

```bash
# Set environment variables
export DB_USERNAME=bizplan
export DB_PASSWORD=your_password
export ENCRYPTION_KEY=your_base64_encoded_32byte_key

# Run with MySQL profile
./gradlew bootRun --args='--spring.profiles.active=mysql'
```

### With AI Engine

```bash
# Start AI Engine (separate terminal)
cd ../bizplan_ai
pip install -r requirements.txt
uvicorn app.main:app --port 8000

# Backend will connect to AI Engine at http://localhost:8000
```

## 📡 API Endpoints

### Projects
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/projects/templates` | Get available templates |
| POST | `/projects` | Create new project |
| GET | `/projects/{id}` | Get project details |
| GET | `/projects` | List user's projects |

### Wizard
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/projects/{id}/wizard/steps` | Save wizard step answers |
| GET | `/projects/{id}/wizard/answers` | Get all wizard answers |
| GET | `/projects/{id}/wizard/steps/{stepId}` | Get specific step answers |

### Business Plan
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/projects/{id}/documents/business-plan/generate` | Generate business plan |
| POST | `/projects/{id}/documents/{docId}/sections/{type}/regenerate` | Regenerate section |
| GET | `/projects/{id}/documents/business-plan/latest` | Get latest version |
| GET | `/projects/{id}/documents/business-plan/versions` | Get all versions |

### Financial
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/projects/{id}/financials/generate` | Generate financial projections |
| GET | `/projects/{id}/financials` | Get saved projections |
| POST | `/financials/preview` | Preview without saving |

### Export
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/projects/{id}/export?format=pdf` | Export as PDF |
| GET | `/projects/{id}/export?format=html` | Export as HTML |
| GET | `/projects/{id}/export/formats` | Get supported formats |

## 📂 Project Structure

```
bizplan_be/
├── src/main/java/com/vibe/bizplan/bizplan_be/
│   ├── api/                    # REST Controllers
│   ├── config/                 # Spring Configurations
│   ├── domain/
│   │   ├── entity/             # JPA Entities
│   │   ├── model/              # Enums & Value Objects
│   │   └── service/            # Business Logic
│   ├── dto/
│   │   ├── request/            # Request DTOs
│   │   └── response/           # Response DTOs
│   └── infrastructure/
│       ├── client/             # External API Clients
│       ├── repository/         # JPA Repositories
│       └── security/           # Security Utils
├── src/main/resources/
│   ├── db/migration/           # Flyway Scripts
│   ├── templates/export/       # PDF Templates
│   └── application.properties
├── docker/                     # Docker Compose files
│   ├── docker-compose.monitoring.yml
│   ├── prometheus/
│   └── grafana/
├── k6/                         # Load Test Scripts
│   └── scenarios/
├── docs/                       # Documentation
└── .cursor/rules/              # AI Agent Rules
```

## 📊 Monitoring

### Start Prometheus & Grafana

```bash
docker-compose -f docker/docker-compose.monitoring.yml up -d

# Prometheus: http://localhost:9090
# Grafana: http://localhost:3000 (admin/bizplan123)
```

### Available Metrics
- JVM metrics (memory, GC, threads)
- HTTP request metrics (duration, count, errors)
- Custom business metrics

## 🧪 Load Testing

```bash
# Install k6
brew install k6  # macOS

# Smoke test (quick validation)
k6 run k6/scenarios/smoke-test.js

# Wizard flow load test
k6 run k6/scenarios/wizard-flow.js

# Document generation test
k6 run k6/scenarios/document-generation.js
```

### Performance Targets (SLA)
| API | Target |
|-----|--------|
| Wizard API | p95 < 800ms |
| Document Generation | p95 < 10s |
| PDF Export | p95 < 5s |

## 🔒 Security

- **Encryption**: AES-256 for sensitive data at rest
- **Password Hashing**: BCrypt
- **TLS**: 1.2+ for all transit (production)
- **Data Isolation**: Workspace-level separation

## 🤝 Contributing

This project follows strict development guidelines:

- **Branching**: Git Flow (`main` → `develop` → `feature/*`)
- **Commits**: Conventional Commits format
- **Code Style**: See `.cursor/rules/` for detailed guidelines

### Key Rules
- [300-java-spring-boot-rules.mdc](.cursor/rules/300-java-spring-boot-rules.mdc) - Java/Spring guidelines
- [303-api-design-rules.mdc](.cursor/rules/303-api-design-rules.mdc) - REST API standards
- [200-git-commit-push-pr.mdc](.cursor/rules/200-git-commit-push-pr.mdc) - Git workflow

## 📝 License

This project is proprietary software. All rights reserved.

---

## 🔗 Related Projects

- [bizplan_ai](https://github.com/voom20/bizplan_ai) - AI Engine (Python/FastAPI)
