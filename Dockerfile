# 使用支持Java 24的基础镜像
FROM openjdk:26-ea-24-jdk-trixie

# 设置工作目录
WORKDIR /app

# 复制Maven构建文件
COPY pom.xml .

# 复制源代码
COPY src ./src

COPY data ./data

# 构建应用
RUN apt-get update && apt-get install -y maven && \
    mvn -Drevision=Docker$(date -u +%Y%m%d%H%M%S) clean package -DskipTests

# 暴露端口
EXPOSE 47631

# 创建volume用于持久化SQLite数据库和上传文件
VOLUME ["/app/data"]

WORKDIR /app/data
# 运行应用
ENTRYPOINT ["java", "-jar", "../target/MaskServer.jar"]