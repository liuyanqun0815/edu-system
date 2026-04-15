# Nacos 配置导入指南（Docker Compose 版本）

本文用于配合根目录 `docker-compose.yml` 启动后的环境，将 `edu-admin` 项目依赖配置导入 Nacos。

## 1. 登录 Nacos 控制台

- 地址：`http://localhost:8848/nacos`
- 账号：`nacos`
- 密码：`nacos`

## 2. 创建配置一：`edu-admin`

- Data ID：`edu-admin`
- Group：`DEFAULT_GROUP`
- 配置格式：`YAML`

内容如下（已改为容器网络地址）：

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://edu-mysql:3306/edu_training?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8
    username: root
    password: 123456
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      filters: stat,wall
      connection-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000

  redis:
    host: edu-redis
    port: 6379
    password:
    database: 0
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms

xxl:
  job:
    admin:
      addresses: http://localhost:8088/xxl-job-admin
    accessToken: default_token
    executor:
      appname: edu-training-executor
      address:
      ip:
      port: 8089
      logpath: /data/applogs/xxl-job/jobhandler
      logretentiondays: 30
```

## 3. 创建配置二：`edu-business`

- Data ID：`edu-business`
- Group：`DEFAULT_GROUP`
- 配置格式：`YAML`

内容如下：

```yaml
edu:
  jwt:
    secret: ${JWT_SECRET:edu-training-jwt-secret-key-2026-change-me}
    expiration: 86400000
    refresh-expiration: 604800000
    token-header: Authorization
    token-prefix: "Bearer "

  rate-limit:
    sms:
      max-count: 5
      window-seconds: 300
    login-fail:
      max-count: 3
      window-seconds: 3600
    api:
      max-count: 60
      window-seconds: 60
    upload:
      max-count: 10
      window-seconds: 3600
    repeat-submit:
      max-count: 60
      window-seconds: 60

  repeat-submit:
    expire-seconds: 3
    header-name: X-Repeat-Submit-Token

  file:
    upload:
      path: doc/uploads/
      url-prefix: /uploads/
      max-size: 10
      allowed-image-types: jpg,jpeg,png,gif,webp

  mail:
    date-format: "yyyy-MM-dd HH:mm:ss"
    template-path: "classpath:email-templates/"
    timeout: 5000
    connection-timeout: 5000

  cors:
    allowed-origins: "*"
    allowed-methods: "GET,POST,PUT,DELETE,OPTIONS"
    allowed-headers: "*"
    allow-credentials: true
    max-age: 3600
```

## 4. 启动与验证

1. 启动：

   ```bash
   docker compose up -d --build
   ```

2. 查看应用日志：

   ```bash
   docker compose logs -f edu-admin
   ```

3. 访问服务：
   - 后端：`http://localhost:8070`
   - 文档：`http://localhost:8070/doc.html`

## 5. 常见错误与修复建议

- 错误：`Nacos config not found`  
  修复：检查 Data ID 是否为 `edu-admin`、`edu-business`，Group 是否为 `DEFAULT_GROUP`。

- 错误：数据库连接失败  
  修复：确认 `edu-admin` 配置里 `spring.datasource.url` 指向 `edu-mysql:3306`，并检查 MySQL 容器健康状态。

- 错误：Redis 连接失败  
  修复：确认 `spring.redis.host=edu-redis`，并执行 `docker compose ps` 检查 Redis 容器状态。
