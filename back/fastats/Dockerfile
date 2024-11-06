# Spring Boot용 Dockerfile

# 빌드 단계
FROM gradle:7.6-jdk17 AS build
WORKDIR /app

# Gradle 캐시 최적화: Gradle 설정 파일만 먼저 복사하고 종속성 다운로드
COPY build.gradle settings.gradle ./
RUN gradle build --no-daemon --parallel --only-if-needed || true

# 나머지 프로젝트 파일 복사 및 빌드
COPY . .
RUN ./gradlew clean bootJar --no-daemon

# 실행 단계
FROM openjdk:17-jdk-slim
WORKDIR /app

# 빌드 결과물 복사
COPY --from=build /app/build/libs/fastats-0.0.1-SNAPSHOT.jar /app/fastats.jar
# # 설정 파일을 복사
# COPY ./application-dev.yml /app/application-dev.yml

# 애플리케이션 실행
# ENTRYPOINT ["java", "-jar", "/app/fastats.jar", "--spring.config.additional-location=classpath:/application-dev.yml"]
ENTRYPOINT ["java", "-jar", "/app/fastats.jar"]
