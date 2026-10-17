FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# serverフォルダ内の中身（Javaファイル等）をすべて丸ごとコピー
COPY . .
# Javaファイルをその場でコンパイルし、そのまま起動
CMD ["sh", "-c", "javac *.java && java GameServer"]
