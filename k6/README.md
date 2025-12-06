# k6 Load Testing

BizPlan Backend API 성능 테스트를 위한 k6 스크립트입니다.

## 📋 성능 목표 (SLA)

| API | 목표 | 측정 기준 |
|-----|------|----------|
| Wizard API | p95 < 800ms | 단계 전환/저장 |
| Document Generation | p95 < 10s | 전체 생성 시간 |
| PDF Export | p95 < 5s | 파일 생성 시간 |

## 🛠️ 설치

### macOS
```bash
brew install k6
```

### Linux (Debian/Ubuntu)
```bash
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6
```

### Docker
```bash
docker pull grafana/k6
```

## 🚀 실행 방법

### 1. 스모크 테스트 (빠른 검증)

```bash
# 로컬 서버 대상
k6 run k6/scenarios/smoke-test.js

# 특정 서버 대상
k6 run -e BASE_URL=https://api.example.com k6/scenarios/smoke-test.js
```

### 2. Wizard Flow 부하 테스트

```bash
# 기본 실행 (점진적 부하 증가)
k6 run k6/scenarios/wizard-flow.js

# 환경변수 설정
k6 run -e BASE_URL=http://localhost:8080 k6/scenarios/wizard-flow.js
```

### 3. 문서 생성 부하 테스트

```bash
k6 run k6/scenarios/document-generation.js
```

### 4. Docker로 실행

```bash
docker run --rm -i \
  -v $(pwd)/k6:/scripts \
  --network host \
  grafana/k6 run /scripts/scenarios/smoke-test.js
```

## 📊 테스트 시나리오

### smoke-test.js
- **목적**: CI/CD 배포 전 빠른 검증
- **VUs**: 1
- **Duration**: 30초
- **체크**: 기본 API 동작 확인

### wizard-flow.js
- **목적**: Wizard API 성능 검증
- **VUs**: 0 → 10 → 50 → 100 → 0 (램프업)
- **Duration**: 4분
- **시나리오**: 프로젝트 생성 → Wizard 5단계 입력 → 조회

### document-generation.js
- **목적**: 문서 생성 성능 검증
- **VUs**: 5 (고정)
- **Duration**: 2분
- **시나리오**: 프로젝트+Wizard 데이터 생성 → 문서 생성 → PDF 내보내기

## 📈 결과 분석

### 콘솔 출력

```
✓ project created (201)
✓ step1 saved (200)
✓ step1 response time OK

     █ 1. 프로젝트 생성
       ✓ project created (201)

     █ 2. Wizard 답변 입력
       ✓ step1 saved (200)
       ...

     checks.........................: 100.00% ✓ 500 ✗ 0
     http_req_duration..............: avg=245ms min=89ms med=198ms max=1.2s p(90)=456ms p(95)=678ms
     wizard_step_duration...........: avg=198ms min=76ms med=165ms max=890ms p(95)=654ms
```

### HTML 리포트 생성

```bash
k6 run --out json=results.json k6/scenarios/wizard-flow.js
```

### InfluxDB + Grafana 연동

```bash
k6 run \
  --out influxdb=http://localhost:8086/k6 \
  k6/scenarios/wizard-flow.js
```

## 🎯 임계값 (Thresholds)

스크립트에 정의된 성능 목표:

```javascript
thresholds: {
    'http_req_duration': ['p(95)<800'],      // 전체 요청 p95 < 800ms
    'wizard_step_duration': ['p(95)<800'],   // Wizard API p95 < 800ms
    'doc_generation_duration': ['p(95)<10000'], // 문서 생성 p95 < 10s
    'errors': ['rate<0.1'],                  // 에러율 < 10%
}
```

## 🔧 CI/CD 통합

### GitHub Actions

```yaml
- name: Run k6 Load Test
  uses: grafana/k6-action@v0.3.0
  with:
    filename: k6/scenarios/smoke-test.js
    flags: -e BASE_URL=${{ secrets.API_URL }}
```

## 📁 파일 구조

```
k6/
├── README.md
└── scenarios/
    ├── smoke-test.js          # 스모크 테스트
    ├── wizard-flow.js         # Wizard 부하 테스트
    └── document-generation.js # 문서 생성 부하 테스트
```

## 💡 팁

1. **로컬 테스트 전** 백엔드 서버가 실행 중인지 확인
2. **AI Engine 연동 테스트** 시 AI Engine도 실행 필요
3. **부하 테스트** 시 DB 연결 수 및 메모리 모니터링 권장
4. **운영 환경 테스트** 시 별도의 테스트 계정/데이터 사용

