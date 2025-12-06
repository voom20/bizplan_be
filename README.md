# AI Co-Pilot for First-time Founders (Bizplan Backend)

## 📖 Project Overview

**Bizplan**은 복잡한 사업계획서 작성 과정을 데이터 기반 의사결정 여정으로 변환하는 지능형 파트너입니다. 처음 창업하는 창업자와 소상공인이 정부 지원금, 은행 대출 등 펀딩 관문을 빠르게 통과하고 지속 가능한 성장에 집중할 수 있도록 돕습니다.

### 🎯 Vision
> "복잡한 사업계획 작성 과정을 데이터 기반 의사결정 여정으로 전환하여 실패율을 줄입니다."

## ✨ Key Features

- **🚀 Submission Wizard**
  - 정부 및 은행 양식에 맞춘 단계별 가이드
  - 자동 저장 및 진행률 추적
  - 100% 템플릿 호환성

- **💰 Financial Auto-Engine**
  - 핵심 변수 기반 3년 손익계산서 및 현금흐름 예측
  - 유닛 이코노믹스(LTV, CAC, BEP) 자동 계산
  - 결정론적 규칙 기반 엔진 (환각 없음)

- **🤖 AI Drafting (Co-Pilot)**
  - **Google Gemini** & **LangChain** 기반 문맥 인식 작성 지원
  - 'Easy' 및 'Expert' 모드 지원
  - 섹션별 재생성 기능

- **📊 PMF Diagnostic**
  - 표준 프레임워크 기반 Product-Market Fit 분석
  - 리스크 식별 및 권장 사항 제공

- **📄 Document Export**
  - 전문적인 포맷의 PDF 내보내기
  - HTML 미리보기 지원
  - (HWP 지원은 향후 릴리스 예정)

## 🛠 Technical Stack

### Backend Core (API Server)
| Component | Technology |
|-----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.8 |
| Build | Gradle 8.x |
| API | REST (OpenAPI 3.0 + SpringDoc) |
| Database | MySQL 8.x / H2 (dev) |
| Migration | Flyway |
| Security | Spring Security, AES-256 |
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
| Logging | Logstash (JSON structured) |
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
| GET | `/projects/templates` | 사용 가능한 템플릿 목록 조회 |
| POST | `/projects` | 새 프로젝트 생성 |
| GET | `/projects/{id}` | 프로젝트 상세 정보 조회 |
| GET | `/projects` | 내 프로젝트 목록 조회 |

### Wizard
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/projects/{id}/wizard/steps` | Wizard 단계별 답변 저장 |
| GET | `/projects/{id}/wizard/answers` | 전체 Wizard 답변 조회 |
| GET | `/projects/{id}/wizard/steps/{stepId}` | 특정 단계 답변 조회 |

### Business Plan Documents
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/projects/{id}/documents/business-plan/generate` | 사업계획서 전체 생성 |
| POST | `/projects/{id}/documents/{docId}/sections/{type}/regenerate` | 특정 섹션 재생성 |
| GET | `/projects/{id}/documents/business-plan/latest` | 최신 버전 문서 조회 |
| GET | `/projects/{id}/documents/business-plan/versions` | 모든 버전 목록 조회 |
| GET | `/projects/{id}/documents/{docId}` | 특정 문서 조회 |

### Financials
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/projects/{id}/financials/generate` | 재무 추정 생성 (저장) |
| POST | `/financials/preview` | 재무 추정 미리보기 (저장 안함) |

### Export
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/projects/{id}/export?format=pdf` | PDF로 내보내기 |
| GET | `/projects/{id}/export?format=html` | HTML로 내보내기 |
| GET | `/projects/{id}/export/versions/{version}` | 특정 버전 내보내기 |
| GET | `/projects/{id}/export/formats` | 지원 형식 목록 조회 |

## 📂 Project Structure

```
bizplan_be/
├── src/main/java/com/vibe/bizplan/bizplan_be/
│   ├── api/                    # REST Controllers
│   │   ├── BusinessPlanController.java
│   │   ├── ExportController.java
│   │   ├── FinancialController.java
│   │   ├── FinancialPreviewController.java
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ProjectController.java
│   │   └── WizardController.java
│   ├── config/                 # Spring Configurations
│   │   ├── SecurityConfig.java
│   │   └── ThymeleafConfig.java
│   ├── domain/
│   │   ├── entity/             # JPA Entities
│   │   ├── model/              # Enums & Value Objects
│   │   └── service/            # Business Logic
│   ├── dto/
│   │   ├── request/            # Request DTOs
│   │   └── response/           # Response DTOs
│   └── infrastructure/
│       ├── client/             # External API Clients (AI Engine)
│       ├── repository/         # JPA Repositories
│       └── security/           # AES Encryption Utils
├── src/main/resources/
│   ├── db/migration/           # Flyway Scripts
│   │   ├── V1__create_project_table.sql
│   │   ├── V2__add_wizard_answers_column.sql
│   │   └── V3__create_business_plan_document_table.sql
│   ├── templates/export/       # PDF Templates
│   ├── application.properties
│   └── application-mysql.properties
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

### Actuator Endpoints
| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | 헬스 체크 |
| `/actuator/info` | 애플리케이션 정보 |
| `/actuator/metrics` | 메트릭 조회 |
| `/actuator/prometheus` | Prometheus 메트릭 |

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
- **TLS**: 1.2+ for all transit (production)
- **Data Isolation**: Workspace-level separation
- **Environment Variables**: 운영 환경에서는 반드시 환경변수로 암호화 키 설정

### Environment Variables (Production)
```bash
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_db_password
export ENCRYPTION_KEY=your_base64_encoded_32byte_key
export AI_ENGINE_URL=http://your-ai-engine:8000
```

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
