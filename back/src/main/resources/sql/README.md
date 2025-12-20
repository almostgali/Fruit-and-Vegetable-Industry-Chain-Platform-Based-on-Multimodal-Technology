# SQL 目录说明

此目录包含本项目所有数据库初始化与结构脚本，统一使用 `fruit_recognition` 数据库。

包含文件：
- `logistics_management.sql`：物流公司、订单、追踪、仓库与统计表结构及初始化数据。
- `logistics_admin.sql`：物流管理员表及示例数据。
- `user.sql`：用户表结构。
- `fruit_quality_detection.sql`：水果品质检测结果表结构。
- `fruit_maturity_detection.sql`：水果成熟度检测结果表结构。

初始化顺序建议：
1. 创建数据库并授予权限（如未存在）。
2. 依次执行：`logistics_management.sql` → `logistics_admin.sql` → `user.sql` → `fruit_quality_detection.sql` → `fruit_maturity_detection.sql`。

执行方式：
- MySQL 客户端：`mysql -u root -p < 文件.sql`
- 或在 MySQL 控制台中 `SOURCE 文件完整路径;`
- Windows 批处理：在项目根目录运行 `mysql-setup.bat` 将按推荐顺序执行。

补充说明：
- 若根目录存在 `setup-database-fixed.sql` 或批处理脚本（如 `mysql-setup.bat`），请优先参考本目录脚本，并确保指向 `back/src/main/resources/sql` 下的这些文件。
- 如需自动初始化，已在 `application.properties` 配置 `spring.sql.init.schema-locations` 指向本目录脚本，应用启动时会执行。
- 已开启 MyBatis 下划线到驼峰映射：`mybatis.configuration.map-underscore-to-camel-case=true`，确保如 `create_time` 能映射到 `createTime`。

注意事项：
- 若需变更数据库名，请同步修改 `application.properties` 中的 `spring.datasource.url`。
- 所有表名与代码中的实体/Mapper 对齐，避免命名不一致造成运行错误。