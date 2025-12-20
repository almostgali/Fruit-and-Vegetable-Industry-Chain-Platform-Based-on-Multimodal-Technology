# 项目进展PPT大纲（智慧果蔬识别与物流管理平台）

## 1. 封面
- 项目名称、团队/负责人、日期

## 2. 项目背景与目标
- 痛点：果蔬质量检测与物流管理数据分散、流程割裂
- 目标：打通“检测-下单-追踪-统计”全链路，提高效率与透明度

## 3. 技术路线
- 后端：Spring Boot 3.1、Java 21、MyBatis-Plus、MySQL、WebSocket
- 前端：Thymeleaf、Axios、HTML/CSS/JS
- 架构图：前后端交互与数据库、WebSocket数据流

## 4. 核心模块
- 用户登录与会话管理
- 模型检测（品质/新鲜度）与结果入库
- 物流订单管理（创建、列表、状态更新、批量导入）
- 溯源追踪与实时大屏

## 5. 目前进展
- API对齐与联调完成（创建订单、订单列表、统计接口正常）
- 前端路径修复、`logistics-management.js` 与后端一致
- Session校验与管理员登录页面展示正确
- 大屏与统计接口返回数据正常

## 6. 演示截图（建议）
- 订单创建页、订单列表、统计卡片、大屏展示、溯源页面

## 7. 关键接口概览（节选）
- `POST /api/logistics/order`
- `GET /api/logistics/order/list`
- `PUT /api/logistics/order/{id}/status`
- `POST /api/model/detect/quality`
- `GET /api/logistics/dashboard/realtime`

## 8. 数据库设计要点
- `logistics_order`、`logistics_track`、`warehouse`、`warehouse_stock`、`logistics_statistics`
- 分页查询与省市统计、时间序列数据支持

## 9. 风险与问题
- 前端缓存导致的加载异常（已定位并修复路径问题，后续加拦截器）
- 数据质量与批量导入校验
- WebSocket推送稳定性与并发控制

## 10. 下一步计划（路线图）
- 前端：状态变更交互优化、批量导入CSV/Excel、统一错误提示
- 后端：参数校验与异常处理、统计接口丰富、刷新大屏数据定时任务
- 部署：撰写部署与运维文档、环境变量与端口化配置

## 11. 里程碑
- M1：核心模块联通与联调完成（当前）
- M2：批量导入与异常处理完善
- M3：大屏与统计全面上线，增加自动推送

## 12. 致谢
- 团队成员与贡献者