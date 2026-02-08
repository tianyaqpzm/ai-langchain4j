# Database Migration Scripts

本目录用于管理 ai-langchain4j 项目的数据库版本迁移脚本。

## 命名规范

```
V<version>__<description>.sql
```

- `V1.0__init_schema.sql` - 初始化表结构
- `V1.1__add_user_table.sql` - 添加用户表
- `V2.0__breaking_change.sql` - 大版本变更

## 版本历史

| 版本 | 文件 | 描述 | 日期 |
|------|------|------|------|
| 1.0 | V1.0__init_schema.sql | 初始化 chat_messages 表 | 2026-02-09 |

## 执行方式

### 手动执行
```bash
psql -h <host> -U <user> -d <database> -f V1.0__init_schema.sql
```

### 使用 Flyway (推荐)
如需自动化迁移，可添加 Flyway 依赖：
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```
