# 第一阶段：编译、打包
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /build

# 复制 Maven Wrapper 和项目配置
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# 复制源代码
COPY src/ src/

# 使用项目声明的 Maven 版本打包，跳过测试执行
RUN sh ./mvnw -B -DskipTests package


# 第二阶段：运行应用
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 创建普通用户，避免以 root 运行应用
RUN groupadd --system app \
    && useradd --system --gid app --no-create-home app

# 只从构建阶段复制打包好的 JAR
COPY --from=builder --chown=app:app /build/target/*.jar /app/app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]