# 1. 빌드 스테이지
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
RUN chmod +x ./gradlew
# 의존성 먼저 캐싱
RUN ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew clean build -x test --no-daemon

# 2. 실행 스테이지
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 로그 디렉토리 생성 및 권한 설정
RUN mkdir -p /app/logs && \
    chmod 755 /app/logs

COPY --from=build /app/build/libs/*.jar app.jar

# 환경 변수 설정
ENV TZ=Asia/Seoul
ENV LOG_PATH=/app/logs
ENV LOG_FILE=dotori

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
