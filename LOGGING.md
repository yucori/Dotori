# 로그 파일 관리 가이드

## 로그 파일 구조

애플리케이션은 다음 세 가지 로그 파일을 생성합니다:

1. **dotori-critical.log**: 중요한 로그 (인증, 보안 관련)
   - 동기적으로 즉시 파일에 기록
   - 최대 보관 기간: 30일
   - 최대 크기: 1GB

2. **dotori-error.log**: 에러 레벨 로그만
   - 동기적으로 즉시 파일에 기록
   - 최대 보관 기간: 30일
   - 최대 크기: 500MB

3. **dotori.log**: 일반 로그
   - 비동기적으로 기록 (성능 최적화)
   - 최대 보관 기간: 30일
   - 최대 크기: 1GB

## 로그 파일 위치

### 로컬 개발 환경
- 기본 경로: `./logs/`
- 환경 변수로 변경 가능: `LOG_PATH`, `LOG_FILE`

### Docker 환경
- 컨테이너 내부: `/app/logs/`
- 호스트 마운트: `./logs/` (docker-compose.yml에서 볼륨 마운트)

## 로그 로테이션

로그 파일은 다음 정책에 따라 자동으로 로테이션됩니다:

- **일별 로테이션**: 매일 자정에 새로운 파일 생성
- **크기 기반 로테이션**: 파일 크기가 100MB를 초과하면 로테이션
- **보관 기간**: 최대 30일간 보관 후 자동 삭제
- **총 크기 제한**: 
  - critical: 1GB
  - error: 500MB
  - 일반: 1GB

## Docker에서 로그 관리

### 볼륨 마운트

`docker-compose.yml`에서 로그 디렉토리를 호스트에 마운트합니다:

```yaml
volumes:
  - ./logs:/app/logs
```

이렇게 하면:
- 컨테이너가 재시작되어도 로그 파일이 유지됩니다
- 호스트에서 직접 로그 파일에 접근할 수 있습니다
- 로그 파일 백업이 용이합니다

### 로그 확인 방법

```bash
# 실시간 로그 확인
tail -f logs/dotori.log

# 에러 로그만 확인
tail -f logs/dotori-error.log

# 중요 로그 확인
tail -f logs/dotori-critical.log

# 특정 날짜 로그 확인
cat logs/dotori-2024-01-15.log
```

### 로그 파일 정리

오래된 로그 파일은 자동으로 삭제되지만, 수동으로 정리할 수도 있습니다:

```bash
# 7일 이상 된 로그 파일 삭제
find logs/ -name "*.log" -mtime +7 -delete

# 특정 크기 이상의 로그 파일 확인
du -sh logs/*
```

## 환경 변수 설정

### 로그 경로 변경

```bash
# 로컬 실행 시
export LOG_PATH=/var/log/dotori
export LOG_FILE=dotori
java -jar app.jar

# Docker 실행 시 (docker-compose.yml)
environment:
  - LOG_PATH=/app/logs
  - LOG_FILE=dotori
```

## 로그 레벨 설정

로그 레벨은 `logback-spring.xml`에서 설정됩니다:

- **인증/보안**: DEBUG
- **예외 핸들러**: INFO
- **일반 비즈니스 로직**: DEBUG
- **루트 로거**: INFO

프로덕션 환경에서는 `application.yml`에서 로그 레벨을 조정할 수 있습니다.

## 모니터링 및 알림

프로덕션 환경에서는 다음을 고려하세요:

1. **로그 집계 도구**: ELK Stack, Splunk, CloudWatch 등
2. **에러 알림**: 에러 로그 모니터링 및 알림 설정
3. **로그 분석**: 정기적인 로그 분석으로 문제 사전 감지
4. **백업**: 중요한 로그 파일의 정기적인 백업

## 주의사항

1. **디스크 공간**: 로그 파일이 디스크 공간을 차지하므로 정기적으로 모니터링하세요
2. **권한**: 로그 디렉토리에 대한 적절한 권한 설정이 필요합니다
3. **성능**: 비동기 로거를 사용하더라도 디스크 I/O가 발생하므로 모니터링하세요
4. **보안**: 로그 파일에 민감한 정보가 포함될 수 있으므로 접근 권한을 제한하세요

