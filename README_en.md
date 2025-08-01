# Apache InLong - Audit Tool

The `inlong-audit-tool` is a standalone service responsible for auditing data consistency across different components of the InLong system. It periodically fetches audit data, compares them based on configurable policies, and reports any discrepancies to monitoring platforms like Prometheus and OpenTelemetry.

## Features

- **Policy-based Auditing**: Configure audit policies in `inlong-manager` to define how data should be compared.
- **Pluggable Reporters**: Easily extend the tool to report alarms to different platforms. Currently supports Prometheus and OpenTelemetry (logging).
- **Standalone Service**: Runs independently and can be deployed anywhere with access to the `inlong-manager` and `inlong-audit` databases.

## Configuration

The service requires connections to two databases:
1.  **InLong Manager Database**: To fetch alarm policies.
2.  **InLong Audit Database**: To query audit data.

Database connections and other settings can be configured in the `application.properties` file located in the `src/main/resources` directory.

### `application.properties`

```properties
# Manager DataSource settings
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/apache_inlong_manager
spring.datasource.username=root
spring.datasource.password=inlong

# Audit DataSource settings
audit.datasource.url=jdbc:mysql://127.0.0.1:3306/apache_inlong_audit
audit.datasource.username=root
audit.datasource.password=inlong

# Prometheus server port
# server.port=9090 # This is the default prometheus port, you can change it if needed.
```

## How It Works

1.  **Policy Fetching**: The `AuditDataChecker` task runs periodically (every minute by default) and fetches all active audit alarm policies from `inlong-manager`.
2.  **Data Querying**: For each policy, it queries the `audit_data` table in the audit database for the specified `audit_ids`.
3.  **Comparison**: It compares the data based on the `alarm_type` (e.g., `THRESHOLD`, `CHANGE_RATE`) and `trigger_type` (e.g., `GREATER_THAN`) defined in the policy.
4.  **Reporting**: If a discrepancy is found that meets the policy conditions, it uses the configured `notification_type` to report the alarm. For example, it will expose a metric to the Prometheus server.

## How to Run

Build the module with Maven, and then run the generated JAR file:

```bash
mvn clean package
java -jar target/inlong-audit-tool-2.3.0-SNAPSHOT.jar
``` 