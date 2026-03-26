# Этап 1: Сборка проекта (Builder)
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Копируем файлы Gradle
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle* ./

# Даем права на выполнение Gradle Wrapper
RUN chmod +x ./gradlew

# Копируем исходный код
COPY src ./src

# Собираем .jar файл (пропускаем тесты для скорости)
RUN ./gradlew clean bootJar -x test

# Этап 2: Запуск (Runtime) - берем только легкую JRE
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Копируем готовый jar из первого этапа
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]