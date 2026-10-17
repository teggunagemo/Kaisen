# 1. ビルド環境（Java 17 と Maven を用意）
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. 実行環境（軽量な Java 実行環境を用意）
FROM eclipse-temurin:17-jre
WORKDIR /app
# target フォルダ内の jar ファイルをアプリとしてコピー
COPY --from=build /app/target/*.jar app.jar

# ポート番号の設定（SnapDeploy標準）
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
