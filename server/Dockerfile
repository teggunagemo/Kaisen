# --- STEP 1: ビルド環境 ---
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# テストをスキップして確実にパッケージング
RUN mvn clean package -DskipTests

# --- STEP 2: 実行環境 ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# targetフォルダ内にある、末尾が .jar で終わる成果物を一括でapp.jarとしてコピー（名前の不一致を防ぐ）
COPY --from=build /app/target/*.jar app.jar

EXPOSE 10000
CMD ["java", "-jar", "app.jar"]
