#!/bin/sh
set -e  # 命令失败时立即退出

# 环境变量默认值处理
: "${APP_PORT:=8080}"       # 如果未设置 APP_PORT，则默认为 8080
: "${LOG_LEVEL:=INFO}"     # 日志级别默认 INFO

# Nacos 客户端 JVM 配置
export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS} \
  -Duser.home=/tmp \
  -Djm.snapshot.path=/tmp/nacos \
  -Dcom.alibaba.nacos.client.naming.cache.dir=/tmp/nacos \
  -Dnacos.remote.client.grpc.channel.capability.negotiation.timeout=10000 \
  -Dnacos.remote.client.grpc.server.check.timeout=10000 \
  -Dnacos.remote.client.grpc.timeout=10000"

# 输出环境信息
echo "Starting application with PORT=$APP_PORT and LOG_LEVEL=$LOG_LEVEL"

# 执行初始化操作（如数据库迁移）
if [ "$RUN_MIGRATIONS" = "true" ]; then
    echo "Running database migrations..."
    # 这里可以添加实际的迁移命令，例如：
    # java -jar migration-tool.jar
fi

# 启动应用
# 注意：这一步非常关键，它会执行 Dockerfile 里的 CMD 指令
echo "Starting main application..."
exec "$@"