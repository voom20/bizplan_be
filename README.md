# AI Co-Pilot for First-time Founders (Bizplan Backend)

## 📖 Project Overview
**Bizplan** is an intelligent partner designed to transform the complex business planning process into a data-driven decision-making journey. It helps first-time founders and small business owners quickly create high-quality business plans to pass funding gates (government grants, loans) and focus on sustainable growth.

### 🎯 Vision
> "Transform the complex business planning process into a data-driven decision-making journey that reduces failure rates."

## ✨ Key Features

- **🚀 Submission Wizard**
  - Step-by-step guide tailored for government and bank forms.
  - Ensures 100% template compatibility.

- **💰 Financial Auto-Engine**
  - Automatically generates 3-year P&L and cash flow statements from key business variables.
  - Deterministic rule-based engine for accuracy (No hallucinations).

- **🤖 AI Drafting (Co-Pilot)**
  - Context-aware writing assistance using **Google Gemini** & **LangChain**.
  - Supports 'Easy' and 'Expert' modes for different user needs.

- **📊 PMF Diagnostic**
  - Analyzes Product-Market Fit and risks based on standard frameworks.

- **📄 Docs Export**
  - One-click export to **HWP** and **PDF** formats, fully compliant with submission standards.

## 🛠 Technical Stack

### Backend Core (API Server)
- **Language:** Java 17
- **Framework:** Spring Boot 3.x
- **Build Tool:** Gradle
- **API Style:** REST (OpenAPI 3.0)
- **Testing:** JUnit 5, Mockito

### AI/Doc Engine (Microservice)
- **Language:** Python 3.10+
- **Framework:** FastAPI
- **AI Orchestration:** LangChain
- **LLM:** Google Gemini
- **Testing:** Pytest

### Data & Infrastructure
- **Database:** MySQL 8.x (InnoDB)
- **Migration:** Flyway
- **Container:** Docker, Docker Compose
- **Cloud:** AWS (S3, RDS)

## 🚀 Getting Started

### Prerequisites
- Java 17 (JDK)
- Docker & Docker Compose
- MySQL 8.0+

### Installation & Running

1. **Clone the repository**
   ```bash
   git clone https://github.com/voom20/bizplan_be.git
   cd bizplan_be
   ```

2. **Build the Project**
   ```bash
   ./gradlew build
   ```

3. **Run the Application**
   ```bash
   ./gradlew bootRun
   ```

## 📂 Project Structure

```
bizplan_be/
├── src/
│   ├── main/java/       # Core Backend Logic (Spring Boot)
│   └── main/resources/  # Configuration & Properties
├── docs/                # Project Documentation (PRD, SRS)
├── .cursor/rules/       # AI Agent & Project Rules
└── build.gradle         # Project Dependencies
```

## 🤝 Contributing

This project follows strict development guidelines to ensure reliability and security.
Please refer to the documentation in `.cursor/rules/` before making changes.

- **Branching:** Git Flow / Feature Branch Workflow
- **Commit Style:** Conventional Commits
- **Java Rules:** [300-java-spring-boot-rules.mdc](.cursor/rules/300-java-spring-boot-rules.mdc)
- **API Rules:** [303-api-design-rules.mdc](.cursor/rules/303-api-design-rules.mdc)

## 📝 License

This project is proprietary software. All rights reserved.
