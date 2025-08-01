# Apache InLong Audit Tool

## 📖 项目简介

Apache InLong Audit Tool 是一个用于审计数据一致性检查的运维工具模块，为 Apache InLong 项目提供数据质量监控和告警功能。

## ✨ 主要功能

### 🔍 数据一致性检查
- **双数据源支持**：同时连接 InLong Manager 和 Audit 数据库
- **定时检查**：每分钟自动检查审计数据差异
- **多种告警类型**：支持阈值告警和变化率告警
- **灵活触发条件**：支持大于、小于等触发条件

### 🚨 告警管理
- **告警策略管理**：通过 REST API 管理告警策略
- **可插拔报告器**：支持 Prometheus 和 OpenTelemetry
- **实时监控**：提供详细的监控指标和健康检查

### 🌐 Web 界面
- **主页**：显示应用信息和可用端点
- **健康检查**：实时监控应用状态
- **错误处理**：友好的错误页面和错误信息
- **REST API**：完整的 API 接口

## 🏗️ 技术架构

### 项目结构
```
Apache-Inlong-Auditmanager/
├── src/main/java/org/apache/inlong/audit/tool/
│   ├── AuditToolApplication.java          # 主启动类
│   ├── config/                            # 配置类
│   │   ├── DataSourceConfig.java          # 双数据源配置
│   │   ├── PrometheusConfig.java          # Prometheus 配置
│   │   └── OpenTelemetryConfig.java       # OpenTelemetry 配置
│   ├── controller/                        # 控制器层
│   │   ├── HomeController.java            # 主页控制器
│   │   ├── CustomErrorController.java     # 错误处理控制器
│   │   └── AuditAlarmPolicyController.java # 审计告警策略控制器
│   ├── dao/                               # 数据访问层
│   │   ├── entity/                        # 实体类
│   │   │   └── AuditData.java             # 审计数据实体
│   │   └── mapper/                        # MyBatis 映射器
│   │       └── AuditDataMapper.java       # 审计数据映射器
│   ├── service/                           # 服务层
│   │   ├── AlarmPolicyService.java        # 告警策略服务
│   │   ├── ReporterFactory.java           # 报告器工厂
│   │   ├── PrometheusReporter.java        # Prometheus 报告器
│   │   └── OpenTelemetryReporter.java     # OpenTelemetry 报告器
│   └── task/                              # 定时任务
│       └── AuditDataChecker.java          # 数据一致性检查任务
└── src/main/resources/
    ├── application.properties             # 应用配置
    └── mappers/                           # MyBatis XML 映射文件
        └── AuditDataMapper.xml
```

### 核心组件

#### 1. 双数据源配置
- **Manager 数据源**：连接 InLong Manager 数据库
- **Audit 数据源**：连接 InLong Audit 数据库
- **MyBatis 多数据源**：支持不同包使用不同的数据源

#### 2. 数据一致性检查任务
- **定时执行**：每分钟检查一次数据差异
- **策略驱动**：根据配置的告警策略进行检查
- **智能告警**：支持阈值和变化率两种告警类型

#### 3. 可插拔告警报告
- **Prometheus 报告器**：上报监控指标
- **OpenTelemetry 报告器**：上报分布式追踪
- **工厂模式**：支持扩展新的报告器类型

## 🚀 快速开始

### 环境要求
- Java 17+
- Maven 3.6+
- MySQL 5.7+

### 1. 克隆项目
```bash
git clone https://github.com/您的用户名/inlong-audit-tool.git
cd inlong-audit-tool
```

### 2. 配置数据库
修改 `src/main/resources/application.properties`：

```properties
# Manager DataSource settings
spring.datasource.url=jdbc:mysql://localhost:3306/apache_inlong_manager?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password

# Audit DataSource settings
audit.datasource.url=jdbc:mysql://localhost:3306/apache_inlong_audit?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
audit.datasource.username=your_username
audit.datasource.password=your_password
```

### 3. 构建项目
```bash
mvn clean package -DskipTests
```

### 4. 运行应用
```bash
java -jar target/inlong-audit-tool-2.3.0-SNAPSHOT.jar
```

### 5. 访问应用
- **主页**：http://localhost:8080/
- **健康检查**：http://localhost:8080/health
- **应用信息**：http://localhost:8080/info
- **审计告警策略**：http://localhost:8080/api/audit/alarm/list

## 📊 监控和告警

### Prometheus 指标
访问 `http://localhost:8080/actuator/prometheus` 获取监控指标：

```yaml
# 告警计数器
audit_alarm_total{policy_id="1",group_id="test_group",stream_id="test_stream"} 5
```

### 健康检查
访问 `http://localhost:8080/actuator/health` 查看应用健康状态：

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    }
  }
}
```

## 🔧 配置说明

### 告警策略配置
通过 REST API 管理告警策略：

```bash
# 获取所有告警策略
curl http://localhost:8080/api/audit/alarm/list

# 添加告警策略（需要实现 POST 接口）
curl -X POST http://localhost:8080/api/audit/alarm/add \
  -H "Content-Type: application/json" \
  -d '{
    "name": "test-policy",
    "alarmType": "THRESHOLD",
    "triggerType": "GREATER_THAN",
    "threshold": 1000,
    "auditIds": "audit1,audit2",
    "inlongGroupId": "test_group",
    "inlongStreamId": "test_stream",
    "notificationType": "PROMETHEUS",
    "enabled": true
  }'
```

### 定时任务配置
修改 `AuditDataChecker.java` 中的 cron 表达式：

```java
@Scheduled(cron = "0 0/1 * * * ?") // 每分钟执行一次
// 可以修改为其他频率，如：
// "0 0/5 * * * ?" // 每5分钟执行一次
// "0 0 0/1 * * ?" // 每小时执行一次
```

## 🧪 测试

### 单元测试
```bash
mvn test
```

### 集成测试
1. 启动 MySQL 数据库
2. 创建测试数据
3. 运行应用
4. 访问测试端点

### 性能测试
- 大数据量测试：插入大量测试数据
- 并发测试：模拟多个并发请求
- 压力测试：测试系统稳定性

## 📝 API 文档

### 主要端点

| 端点 | 方法 | 描述 |
|------|------|------|
| `/` | GET | 主页，显示应用信息 |
| `/health` | GET | 健康检查 |
| `/info` | GET | 应用详细信息 |
| `/api/audit/alarm/list` | GET | 获取所有告警策略 |
| `/actuator/health` | GET | Spring Boot 健康检查 |
| `/actuator/prometheus` | GET | Prometheus 指标 |
| `/actuator/metrics` | GET | 应用指标 |

### 错误处理
访问不存在的路径会返回错误信息：

```json
{
  "timestamp": 1640995200000,
  "status": 404,
  "error": "Not Found",
  "message": "No message available",
  "path": "/nonexistent",
  "available_endpoints": [
    "/ - Home page",
    "/health - Health check",
    "/info - Application info"
  ]
}
```

 

