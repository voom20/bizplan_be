/**
 * k6 Load Test: Document Generation
 * 
 * 시나리오: 사업계획서 생성 API 성능 테스트
 * 목표: Document Generation p95 < 10s
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// ==========================================
// Configuration
// ==========================================

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// 커스텀 메트릭
const docGenerationDuration = new Trend('doc_generation_duration');
const exportDuration = new Trend('export_duration');
const errorRate = new Rate('errors');

// 테스트 옵션 (문서 생성은 리소스 집약적이므로 낮은 동시성)
export const options = {
    scenarios: {
        document_generation: {
            executor: 'constant-vus',
            vus: 5,           // 동시 사용자 5명
            duration: '2m',    // 2분 동안 실행
        },
    },
    thresholds: {
        'doc_generation_duration': ['p(95)<10000'],  // p95 < 10s
        'export_duration': ['p(95)<5000'],           // PDF 내보내기 p95 < 5s
        'errors': ['rate<0.2'],                      // 에러율 < 20%
    },
};

// ==========================================
// Test Data
// ==========================================

const WIZARD_ANSWERS = {
    problem_definition: {
        problem: '중소기업의 업무 자동화 부재로 인한 생산성 저하',
        target_customer: '직원 10-100명 규모의 중소기업',
        pain_points: ['수작업 반복', '데이터 분산', '협업 어려움'],
    },
    solution: {
        product_name: 'BizFlow AI',
        description: 'AI 기반 업무 자동화 및 협업 플랫폼',
        key_features: ['워크플로 자동화', '문서 관리', '실시간 협업'],
    },
    market: {
        tam: '500억원',
        sam: '100억원',
        som: '10억원',
        growth_rate: '15%',
    },
    business_model: {
        revenue_model: '월간 구독',
        pricing_tiers: ['Basic: 3만원/월', 'Pro: 10만원/월', 'Enterprise: 협의'],
    },
    team: {
        ceo: '김대표 (10년 IT 경력)',
        cto: '이기술 (네이버/카카오 출신)',
        team_size: 5,
    },
};

// ==========================================
// Helper Functions
// ==========================================

function makeRequest(method, path, body = null, timeout = '30s') {
    const url = `${BASE_URL}${path}`;
    const params = {
        headers: { 'Content-Type': 'application/json' },
        timeout: timeout,
    };

    if (method === 'GET') {
        return http.get(url, params);
    } else if (method === 'POST') {
        return http.post(url, body ? JSON.stringify(body) : null, params);
    }
}

function createProjectWithWizardData() {
    // 프로젝트 생성
    const projectRes = makeRequest('POST', '/projects', {
        templateCode: 'KSTARTUP_2025',
        title: `성능테스트 프로젝트 ${Date.now()}`,
    });

    if (projectRes.status !== 200 && projectRes.status !== 201) {
        console.log(`Failed to create project: ${projectRes.status}`);
        return null;
    }

    let projectId;
    try {
        projectId = JSON.parse(projectRes.body).projectId;
    } catch {
        return null;
    }

    // Wizard 답변 저장
    for (const [stepId, answers] of Object.entries(WIZARD_ANSWERS)) {
        makeRequest('POST', `/projects/${projectId}/wizard/steps`, {
            stepId: stepId,
            answers: answers,
        });
    }

    return projectId;
}

// ==========================================
// Main Test Scenario
// ==========================================

export default function () {
    let projectId = null;
    let documentId = null;

    group('1. 테스트 데이터 준비', () => {
        projectId = createProjectWithWizardData();
        if (!projectId) {
            errorRate.add(true);
            return;
        }
    });

    if (!projectId) return;

    group('2. 사업계획서 생성', () => {
        const start = Date.now();
        
        // 문서 생성 API 호출 (60초 타임아웃)
        const response = makeRequest(
            'POST',
            `/projects/${projectId}/documents/business-plan/generate`,
            null,
            '60s'
        );
        
        const duration = Date.now() - start;
        docGenerationDuration.add(duration);

        const success = check(response, {
            'document generated (200)': (r) => r.status === 200,
            'generation time < 10s': () => duration < 10000,
            'has document content': (r) => {
                try {
                    const body = JSON.parse(r.body);
                    documentId = body.documentId;
                    return body.sections && Object.keys(body.sections).length > 0;
                } catch {
                    return false;
                }
            },
        });

        errorRate.add(!success);

        if (!success) {
            console.log(`Doc generation failed: ${response.status} (${duration}ms)`);
        } else {
            console.log(`Doc generated successfully in ${duration}ms`);
        }
    });

    if (!documentId) {
        sleep(2);
        return;
    }

    group('3. PDF 내보내기', () => {
        const start = Date.now();
        
        const response = makeRequest(
            'GET',
            `/projects/${projectId}/export?format=pdf`,
            null,
            '30s'
        );
        
        const duration = Date.now() - start;
        exportDuration.add(duration);

        check(response, {
            'PDF exported (200)': (r) => r.status === 200,
            'export time < 5s': () => duration < 5000,
            'has PDF content': (r) => r.body && r.body.length > 1000,
        });
    });

    sleep(2);  // 다음 반복 전 대기
}

// ==========================================
// Lifecycle Hooks
// ==========================================

export function setup() {
    console.log(`Document Generation Load Test`);
    console.log(`Target: ${BASE_URL}`);
    console.log(`Goal: p95 < 10s for document generation`);
    
    return { startTime: Date.now() };
}

export function teardown(data) {
    const duration = (Date.now() - data.startTime) / 1000;
    console.log(`\n=== Test Summary ===`);
    console.log(`Total Duration: ${duration.toFixed(2)}s`);
}

