# --- STEP 1: ビルド環境 ---
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# serverフォルダ内の全ファイルをコンテナにコピー
COPY . .

# Mavenを使ってライブラリを含めてビルド（テストはスキップ）
RUN mvn clean package -DskipTests

# --- STEP 2: 実行環境 ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# targetフォルダ内に生成されたJARファイルをコピー
COPY --from=build /app/target/*.jar app.jar

# ポート開放
EXPOSE 10000

# サーバー起動
CMD ["java", "-jar", "app.jar"]
