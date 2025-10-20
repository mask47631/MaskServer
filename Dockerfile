# 使用支持Java 24的基础镜像
FROM openjdk:24-jdk-slim

# 设置工作目录
WORKDIR /app

# 添加构建时间参数，格式为 yyyyddmmhhmm
ARG BUILD_TIME
ENV BUILD_TIME=${BUILD_TIME:-yyyyMMddHHmm}

# 复制Maven构建文件
COPY pom.xml .

# 复制源代码
COPY src ./src

# 构建应用
RUN apt-get update && apt-get install -y maven && \
    mvn -Drevision=Docker$BUILD_TIME clean package -DskipTests

# 暴露端口
EXPOSE 47631

# 创建volume用于持久化SQLite数据库和上传文件
VOLUME ["/app/uploads", "/app/maskserver.db"]

# 运行应用
ENTRYPOINT ["java", "-jar", "target/MaskServer.jar"]