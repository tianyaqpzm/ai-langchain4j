# ==========================================
# 第一阶段：构建应用
# ==========================================
FROM amazoncorretto:17-alpine3.17 AS build

# 安装 Maven
RUN apk add --no-cache maven

WORKDIR /app

# 缓存依赖
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# 第二阶段：运行应用 (改用带 Shell 的镜像)
# ==========================================
# ⚠️ 关键修改：放弃 distroless，改用 eclipse-temurin 以支持 Shell 脚本
FROM eclipse-temurin:17-jre 

WORKDIR /app

# 1. 创建非 root 用户 (eclipse-temurin 默认通常有 user 1000，为了安全显式创建)
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# 2. 从构建阶段复制 jar 包
COPY --from=build /app/target/ai-langchain4j*.jar /app/ai-langchain4j.jar

# 3. 复制并设置 entrypoint.sh
COPY entrypoint.sh /app/entrypoint.sh

# 4. 赋予执行权限 (同时处理可能的 Windows 换行符问题)
# 如果没有 dos2unix，可以用 sed 替换
RUN chmod +x /app/entrypoint.sh && \
    sed -i 's/\r$//' /app/entrypoint.sh

# 设置环境变量
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxMetaspaceSize=128m -XX:+HeapDumpOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"
ENV APP_PORT=8080

# 声明端口
EXPOSE $APP_PORT

# 5. 健康检查 (Eclipse Temurin 带有 curl，可以直接用)
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:${APP_PORT}/actuator/health || exit 1

# 切换到非 root 用户
USER appuser

# 6. 设置启动命令
# ENTRYPOINT 指向脚本
ENTRYPOINT ["/app/entrypoint.sh"]

# CMD 作为参数传给 ENTRYPOINT 中的 "$@"
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/ai-langchain4j.jar"]