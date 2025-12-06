/**
 * k6 Smoke Test
 * 
 * 빠른 검증을 위한 최소 부하 테스트
 * CI/CD 파이프라인에서 배포 전 검증용
 */

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
    vus: 1,
    duration: '30s',
    thresholds: {
        http_req_duration: ['p(95)<2000'],  // 모든 요청 p95 < 2s
        http_req_failed: ['rate<0.05'],      // 실패율 < 5%
    },
};

export default function () {
    // 1. Health Check
    let response = http.get(`${BASE_URL}/actuator/health`);
    check(response, {
        'health check OK': (r) => r.status === 200,
    });

    // 2. Template List
    response = http.get(`${BASE_URL}/projects/templates`);
    check(response, {
        'templates loaded': (r) => r.status === 200,
    });

    // 3. Project Creation
    const payload = JSON.stringify({
        templateCode: 'KSTARTUP_2025',
        title: `Smoke Test ${Date.now()}`,
    });

    response = http.post(`${BASE_URL}/projects`, payload, {
        headers: { 'Content-Type': 'application/json' },
    });

    check(response, {
        'project created': (r) => r.status === 200 || r.status === 201,
    });

    let projectId = null;
    try {
        projectId = JSON.parse(response.body).projectId;
    } catch (e) {
        console.log('Failed to parse project response');
    }

    if (projectId) {
        // 4. Wizard Step
        response = http.post(
            `${BASE_URL}/projects/${projectId}/wizard/steps`,
            JSON.stringify({
                stepId: 'test_step',
                answers: { test: 'smoke test data' },
            }),
            { headers: { 'Content-Type': 'application/json' } }
        );

        check(response, {
            'wizard step saved': (r) => r.status === 200,
        });

        // 5. Get Project
        response = http.get(`${BASE_URL}/projects/${projectId}`);
        check(response, {
            'project retrieved': (r) => r.status === 200,
        });
    }

    sleep(1);
}

export function handleSummary(data) {
    console.log('\n=== Smoke Test Summary ===');
    console.log(`Total requests: ${data.metrics.http_reqs.values.count}`);
    console.log(`Failed requests: ${data.metrics.http_req_failed.values.passes}`);
    console.log(`p95 response time: ${data.metrics.http_req_duration.values['p(95)']}ms`);
    
    return {
        stdout: JSON.stringify(data.metrics, null, 2),
    };
}

