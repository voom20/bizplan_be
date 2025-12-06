/**
 * k6 Load Test: Wizard Flow
 * 
 * 시나리오: 프로젝트 생성 → Wizard 답변 입력 흐름
 * 목표: Wizard API p95 < 800ms
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// ==========================================
// Configuration
// ==========================================

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// 커스텀 메트릭
const wizardStepDuration = new Trend('wizard_step_duration');
const projectCreateDuration = new Trend('project_create_duration');
const errorRate = new Rate('errors');

// 테스트 옵션
export const options = {
    scenarios: {
        // 점진적 부하 증가
        ramping_load: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 10 },   // 웜업
                { duration: '1m', target: 50 },    // 부하 증가
                { duration: '2m', target: 100 },   // 피크 유지
                { duration: '30s', target: 0 },    // 쿨다운
            ],
            gracefulRampDown: '10s',
        },
    },
    thresholds: {
        // 성능 목표
        'http_req_duration{scenario:ramping_load}': ['p(95)<800'],  // p95 < 800ms
        'wizard_step_duration': ['p(95)<800'],                      // Wizard API p95 < 800ms
        'project_create_duration': ['p(95)<1000'],                  // 프로젝트 생성 p95 < 1s
        'errors': ['rate<0.1'],                                     // 에러율 < 10%
    },
};

// ==========================================
// Test Data
// ==========================================

const TEMPLATES = ['KSTARTUP_2025', 'BANK_LOAN_2025', 'INVESTOR_PITCH_2025'];

const WIZARD_ANSWERS = {
    step1: {
        business_name: '테스트 스타트업',
        business_type: 'B2B SaaS',
        target_market: '중소기업',
    },
    step2: {
        problem: '업무 효율성 저하로 인한 비용 증가',
        severity: 'high',
        affected_users: '100만 명 이상',
    },
    step3: {
        solution: 'AI 기반 업무 자동화 플랫폼',
        key_features: ['자동화', '분석', '리포팅'],
        differentiation: '특허 기술 보유',
    },
    step4: {
        tam: 50000000000,
        sam: 10000000000,
        som: 1000000000,
    },
    step5: {
        revenue_model: '구독형',
        pricing: 'Freemium + Premium',
        arpu: 50000,
    },
};

// ==========================================
// Helper Functions
// ==========================================

function getRandomTemplate() {
    return TEMPLATES[Math.floor(Math.random() * TEMPLATES.length)];
}

function makeRequest(method, path, body = null) {
    const url = `${BASE_URL}${path}`;
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
        timeout: '30s',
    };

    let response;
    if (method === 'GET') {
        response = http.get(url, params);
    } else if (method === 'POST') {
        response = http.post(url, body ? JSON.stringify(body) : null, params);
    }

    return response;
}

// ==========================================
// Main Test Scenario
// ==========================================

export default function () {
    let projectId = null;

    group('1. 프로젝트 생성', () => {
        const payload = {
            templateCode: getRandomTemplate(),
            title: `테스트 프로젝트 ${Date.now()}`,
        };

        const start = Date.now();
        const response = makeRequest('POST', '/projects', payload);
        const duration = Date.now() - start;

        projectCreateDuration.add(duration);

        const success = check(response, {
            'project created (201)': (r) => r.status === 201 || r.status === 200,
            'has projectId': (r) => {
                try {
                    const body = JSON.parse(r.body);
                    projectId = body.projectId;
                    return !!projectId;
                } catch {
                    return false;
                }
            },
        });

        errorRate.add(!success);

        if (!success) {
            console.log(`Project creation failed: ${response.status} - ${response.body}`);
            return;
        }
    });

    if (!projectId) return;

    group('2. Wizard 답변 입력', () => {
        for (const [stepId, answers] of Object.entries(WIZARD_ANSWERS)) {
            const payload = {
                stepId: stepId,
                answers: answers,
            };

            const start = Date.now();
            const response = makeRequest('POST', `/projects/${projectId}/wizard/steps`, payload);
            const duration = Date.now() - start;

            wizardStepDuration.add(duration);

            const success = check(response, {
                [`${stepId} saved (200)`]: (r) => r.status === 200,
                [`${stepId} response time OK`]: () => duration < 800,
            });

            errorRate.add(!success);

            if (!success) {
                console.log(`Wizard ${stepId} failed: ${response.status} - ${response.body}`);
            }

            sleep(0.5);  // 실제 사용자 행동 시뮬레이션
        }
    });

    group('3. Wizard 답변 조회', () => {
        const start = Date.now();
        const response = makeRequest('GET', `/projects/${projectId}/wizard/answers`);
        const duration = Date.now() - start;

        wizardStepDuration.add(duration);

        check(response, {
            'answers retrieved (200)': (r) => r.status === 200,
            'has all steps': (r) => {
                try {
                    const body = JSON.parse(r.body);
                    return body.completedSteps >= 5;
                } catch {
                    return false;
                }
            },
        });
    });

    sleep(1);  // 다음 반복 전 대기
}

// ==========================================
// Lifecycle Hooks
// ==========================================

export function setup() {
    console.log(`Starting load test against: ${BASE_URL}`);
    
    // 서버 헬스체크
    const health = http.get(`${BASE_URL}/actuator/health`);
    if (health.status !== 200) {
        throw new Error(`Server health check failed: ${health.status}`);
    }
    
    console.log('Server is healthy, starting test...');
    return { startTime: Date.now() };
}

export function teardown(data) {
    const duration = (Date.now() - data.startTime) / 1000;
    console.log(`Test completed in ${duration.toFixed(2)} seconds`);
}

