# 个人健康管理与疾病预警系统

一个基于 Spring Boot 的个人健康管理与疾病预警系统，面向个人和家庭成员的健康档案管理、持续记录、风险预警与 AI 辅助分析场景。

## 项目亮点

- 成员档案管理：支持本人、父母、配偶、子女等家庭成员分别建档。
- 健康记录管理：支持体重、腰围、血压、血糖、血氧、睡眠、运动、步数、饮水、情绪、压力、症状、用药等持续记录。
- 风险预警：系统根据健康指标自动计算风险分数并生成预警信息。
- 趋势追踪：围绕体重、收缩压、空腹血糖、睡眠、运动、风险分数进行近期趋势分析。
- 档案详情页：支持点击成员档案，查看完整信息、趋势卡片、最近预警和近期记录。
- AI 咨询：用户手动点击后才调用 DeepSeek，避免无意义消耗 token。
- 前后端一体：Spring Boot 提供 REST 接口，静态前端直接部署于应用内部。

## 技术栈

- 后端：Spring Boot 3.5.11
- 数据访问：Spring Data JPA
- 数据库：H2 文件数据库
- 前端：HTML + CSS + Vanilla JavaScript
- 构建工具：Maven Wrapper

## 当前版本

- 版本号：`v1.0.0`
- 版本主题：个人与家庭健康管理 + 趋势追踪 + 按需 AI 咨询

详细版本说明见 [RELEASE_NOTES.md](./RELEASE_NOTES.md)。

## 运行方式

### 方式一：一键启动

双击项目根目录下的 [start-app.bat](./start-app.bat)。

该脚本会：

- 检查 Java 环境
- 启动 Spring Boot 服务
- 等待服务就绪后自动打开浏览器

### 方式二：命令行启动

在项目根目录执行：

```powershell
.\mvnw.cmd spring-boot:run
```

启动后访问：

- 系统首页：`http://localhost:8081`
- H2 控制台：`http://localhost:8081/h2-console`

## AI 配置说明

出于安全考虑，仓库中不保存真实 API Key。

如需启用 DeepSeek，请先配置环境变量：

```powershell
$env:DEEPSEEK_ENABLED="true"
$env:DEEPSEEK_API_KEY="你的DeepSeekKey"
.\start-app.bat
```

说明：

- AI 不会自动触发
- 只有用户在成员详情页主动点击“调用 AI 分析”按钮时才会发起请求
- 未配置 key 时，系统其余功能依然可以正常使用

## H2 数据库配置

- JDBC URL：`jdbc:h2:file:./data/self-health-care`
- User Name：`sa`
- Password：留空

系统首次启动时会自动写入 3 个示例成员档案与多条健康记录，便于直接演示。

## 主要接口

- `GET /api/dashboard`：仪表盘汇总
- `GET /api/profiles`：成员档案列表
- `GET /api/profiles/{id}/detail`：成员档案详情
- `POST /api/profiles/{id}/ai-consultation`：成员 AI 健康咨询
- `POST /api/profiles`：新增成员档案
- `PUT /api/profiles/{id}`：修改成员档案
- `DELETE /api/profiles/{id}`：删除成员档案
- `GET /api/records`：健康记录列表
- `POST /api/records`：新增健康记录
- `PUT /api/records/{id}`：修改健康记录
- `DELETE /api/records/{id}`：删除健康记录
- `GET /api/alerts`：预警列表
- `PUT /api/alerts/{id}/status`：更新预警状态
- `DELETE /api/alerts/{id}`：删除预警

## 测试

已验证：

```powershell
.\mvnw.cmd test
```

结果为 `BUILD SUCCESS`。
