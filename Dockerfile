FROM eclipse-temurin:26-jdk

WORKDIR /app

# Use root's gradle cache by default so host ~/.gradle can be mounted to /root/.gradle
ENV GRADLE_USER_HOME=/home/gradle/.gradle

# Ensure the gradle home directory exists (will be masked by host mount during dev overrides)
RUN mkdir -p /home/gradle/.gradle

# Gradle Wrapper 및 설정 파일 우선 복사 (의존성 레이어 캐싱)
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Windows-Linux 간 실행 권한 이슈 방지 및 의존성 사전 다운로드
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies

# 소스 코드 복사
COPY src src

EXPOSE 8080

# bootRun으로 앱 실행 (Spring DevTools가 내부 클래스 변경 시 즉시 재시작)
CMD ["./gradlew", "bootRun", "--continuous", "--no-daemon"]