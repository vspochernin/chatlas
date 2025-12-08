# --- СТАДИЯ СБОРКИ ---
# Образ для сборки.
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

# Определяем рабочую директорию.
WORKDIR /app

# Копируем в нее сначала только pom.xml, чтобы кешировались зависимости.
COPY pom.xml ./
# Скачиваем все нужные зависимости в локальный Maven-кэш.
RUN mvn -q -B dependency:go-offline

# Далее копируем исходники.
COPY src ./src

# Собираем jar и копируем зависимости в target/dependency.
RUN mvn -q -B -DskipTests package dependency:copy-dependencies -DoutputDirectory=target/dependency

# --- СТАДИЯ ЗАПУСКА ---
# Образ для запуска.
FROM eclipse-temurin:21-jre-alpine

# Определяем рабочую директорию.
WORKDIR /app

# Создаем пользователя, чтобы запускать НЕ из под root.
RUN adduser -D appuser
USER appuser

# Копируем приложение и зависимости из артефактов стадии сборки.
COPY --from=build /app/target/chatlas-1.0.0-SNAPSHOT.jar app.jar
COPY --from=build /app/target/dependency ./lib

# Определяем точку входа приложения.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp app.jar:lib/* ru.hackathon.chatlas.ChatlasApplication"]
