# Builder Stage
FROM eclipse-temurin:26-jdk AS builder

WORKDIR /app

# Gradle Wrapper 및 설정 파일 우선 복사 (의존성 캐싱)
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies

# 소스코드 복사 및 빌드
COPY src src

EXPOSE 8080

# bootRun으로 앱 실행 (Watch sync 시 DevTools가 즉시 변경 감지)
CMD ["./gradlew", "bootRun", "--no-daemon"]