# PiggyMini 开发文档（可编辑版）

**轻量级个人记账 / 理财微服务系统** —— 基于 Spring Cloud Alibaba + AI 的练手项目

**文档版本**：v1.0

**最后更新**：2026-04-05

**适用编辑器**：Typora/VS Code / 有道云笔记 / 坚果云 Markdown 等主流 Markdown 编辑器

**保存格式**：直接复制内容保存为 `PiggyMini-Development-Doc.md` 放入项目根目录即可使用

## 一、项目概述

### 项目基本信息



* 项目名称：PiggyMini

* 对标项目：PiggyMetrics（轻量化、个人练手向）

* 核心定位：微服务全链路落地练手项目，兼顾**业务实用性** + **技术栈完整性** + **AI 增强**

### 核心功能



1. 用户认证与账户管理

2. 收支记录与分类管理

3. 月度预算与超支预警

4. 数据报表（统计、占比、趋势）

5. 简易投资跟踪

6. AI 智能分类记账（输入文字自动识别收支类型）

## 二、技术栈明细



| 层级    | 技术选型                                              |
| ----- | ------------------------------------------------- |
| 微服务核心 | Spring Cloud Alibaba（Nacos + Gateway + OpenFeign） |
| 基础框架  | Spring Boot 3.x、MyBatis-Plus、Maven 多模块            |
| 数据存储  | MySQL（业务数据）、Redis（分布式缓存）                          |
| 异步通信  | RabbitMQ                                          |
| 云原生部署 | Docker、Minikube（K8s 本地练手）                         |
| 监控运维  | Spring Boot Actuator、Prometheus、Grafana           |
| 特色增强  | Spring AI（对接大模型实现智能记账）                            |
| 辅助工具  | Knife4j（接口文档）、Lombok、JWT、BCrypt                   |

## 三、接口文档（完整版）

### 全局接口规范



1. 统一前缀：所有接口通过 Gateway 访问 → `/api`

2. 认证方式：JWT Bearer Token（**除登录 / 注册外所有接口均需携带**）

3. 全局响应格式：



```
{

&#x20; "code": 200,

&#x20; "message": "success",

&#x20; "data": { ... },

&#x20; "timestamp": "2026-04-05T13:00:00"

}
```



1. 全局状态码：

* `200`：请求成功

* `400`：参数错误

* `401`：未登录 / Token 失效

* `500`：系统内部异常

### 3.1 Auth 服务（piggymini-auth）



| 方法   | 路径                   | 描述       | 请求参数 / Body                  | 响应 data                                         |
| ---- | -------------------- | -------- | ---------------------------- | ----------------------------------------------- |
| POST | `/api/auth/register` | 用户注册     | `{ "username", "password" }` | `{ "userId", "username", "nickname", "token" }` |
| POST | `/api/auth/login`    | 用户登录     | `{ "username", "password" }` | `{ "userId", "token", "refreshToken" }`         |
| POST | `/api/auth/refresh`  | 刷新 Token | `{ "refreshToken" }`         | `{ "token", "refreshToken" }`                   |

### 3.2 Account 服务（piggymini-account）



| 方法     | 路径                   | 描述         | 请求参数 / Body                                               | 响应 data                         |
| ------ | -------------------- | ---------- | --------------------------------------------------------- | ------------------------------- |
| POST   | `/api/accounts`      | 新增账户       | `{ "name", "type"(CASH/BANK/ALIPAY), "balance", "icon" }` | 创建的账户对象                         |
| GET    | `/api/accounts`      | 查询当前用户所有账户 | `?page=1&size=10`                                         | `{ "list": [...], "total": 5 }` |
| GET    | `/api/accounts/{id}` | 查询单个账户     | -                                                         | 账户详情                            |
| PUT    | `/api/accounts/{id}` | 更新账户       | 同新增参数                                                     | 更新后的账户对象                        |
| DELETE | `/api/accounts/{id}` | 删除账户       | -                                                         | `{ "success": true }`           |

### 3.3 Transaction 服务（piggymini-transaction）



| 方法     | 路径                              | 描述          | 请求参数 / Body                                                                         | 响应 data                          |
| ------ | ------------------------------- | ----------- | ----------------------------------------------------------------------------------- | -------------------------------- |
| POST   | `/api/transactions`             | 新增收支记录      | `{ "accountId", "type"(IN/OUT), "amount", "category", "description", "tradeTime" }` | 创建的收支记录对象                        |
| GET    | `/api/transactions`             | 分页查询收支      | `?accountId=&type=&startDate=&endDate=&page=&size=`                                 | 收支记录分页列表                         |
| GET    | `/api/transactions/{id}`        | 查询单条记录      | -                                                                                   | 收支记录详情                           |
| PUT    | `/api/transactions/{id}`        | 修改记录        | 同新增参数                                                                               | 更新后的收支记录对象                       |
| DELETE | `/api/transactions/{id}`        | 删除记录        | -                                                                                   | `{ "success": true }`            |
| POST   | `/api/transactions/ai-classify` | AI 智能分类（异步） | `{ "text": "早餐花了15元" }`                                                             | `{ "taskId": "xxx" }`（或直接返回识别结果） |

### 3.4 Budget 服务（piggymini-budget）



| 方法   | 路径                     | 描述          | 请求参数 / Body                                    | 响应 data                                 |
| ---- | ---------------------- | ----------- | ---------------------------------------------- | --------------------------------------- |
| POST | `/api/budgets`         | 创建 / 更新月度预算 | `{ "month": "2026-04", "category", "amount" }` | 创建 / 更新后的预算对象                           |
| GET  | `/api/budgets/current` | 查询当月所有预算    | -                                              | `[ {category, budget, spent, remain} ]` |
| GET  | `/api/budgets/warn`    | 超支预警列表      | -                                              | 超支预算列表                                  |

### 3.5 Report 服务（piggymini-report）



| 方法  | 路径                              | 描述          | 请求参数 / Body                  | 响应 data                                           |
| --- | ------------------------------- | ----------- | ---------------------------- | ------------------------------------------------- |
| GET | `/api/reports/summary`          | 收支总览（月 / 年） | `?period=month&date=2026-04` | `{ income, expense, balance, trend }`             |
| GET | `/api/reports/category`         | 分类占比        | `?period=month&date=2026-04` | `[ {category, percent, amount} ]`                 |
| GET | `/api/reports/budget-execution` | 预算执行率       | `?month=2026-04`             | 预算执行率列表                                           |
| GET | `/api/reports/trend`            | 收支趋势（时间序列）  | `?startDate=&endDate=`       | `{ dates: [...], income: [...], expense: [...] }` |

### 3.6 Invest 服务（piggymini-invest）



| 方法     | 路径                      | 描述     | 请求参数 / Body                                 | 响应 data               |
| ------ | ----------------------- | ------ | ------------------------------------------- | --------------------- |
| POST   | `/api/investments`      | 新增持仓   | `{ "name", "code", "amount", "costPrice" }` | 创建的持仓对象               |
| GET    | `/api/investments`      | 查询持仓列表 | `?page=1&size=10`                           | 持仓分页列表                |
| PUT    | `/api/investments/{id}` | 更新收益   | `{ "currentPrice" }`                        | 更新后的持仓对象              |
| DELETE | `/api/investments/{id}` | 删除持仓   | -                                           | `{ "success": true }` |

### 3.7 AI 服务（piggymini-ai）—— 内部服务

**对外暴露**：不直接提供接口，仅通过 Transaction 服务的 `/api/transactions/ai-classify` 入口调用

**内部 Feign 接口**（仅供服务间调用）：



* 请求方法：POST

* 接口路径：`/internal/ai/classify`

* 请求参数：`{ "text": "..." }`

* 响应数据：`{ "type": "IN/OUT", "category": "餐饮", "amount": 15, "confidence": 0.95 }`

## 四、开发流程说明（分阶段落地）

### 阶段一：环境搭建（预计 1-2 天）

#### 1. 安装基础开发环境



* JDK 17+

* Maven 3.8+

* IDEA（或 Eclipse）

* Git

* Docker Desktop（用于快速启动中间件，推荐）

#### 2. 启动中间件（推荐 docker-compose 一键启动）



* Nacos（服务注册与配置中心）

* MySQL 8.0+（业务数据存储）

* Redis 6.0+（分布式缓存）

* RabbitMQ 3.12+（异步通信）

#### 3. 创建数据库

每个服务独立建库（也可共用一个库通过表前缀区分），库名如下：

`piggymini_auth`、`piggymini_account`、`piggymini_transaction`、`piggymini_budget`、`piggymini_report`、`piggymini_invest`

### 阶段二：公共模块与基础服务开发（预计 2 天）

#### 1. 开发 piggymini-common 公共模块

封装项目通用能力，包含：



* 统一响应对象 `Result<T>`、全局异常处理器

* JWT 工具类（生成 / 解析 Token）、BCrypt 密码加密工具

* Redis 操作工具类、日期格式化工具类

* 公共实体类（User, Account, Transaction, Budget, Invest）

* 公共枚举（收支类型、账户类型、消费分类等）

#### 2. 开发 piggymini-auth 认证服务



* 实现注册、登录、刷新 Token 核心接口

* 创建用户表 `user`，字段：id, username, password, nickname, create\_time

* 开发 JWT 过滤器（建议放在 Gateway 中做全局校验，也可在 Auth 服务提供校验接口）

#### 3. 初始化数据库表结构（极简版）

以 transaction 表为例，其他表按需设计：



```
\-- 收支记录表

CREATE TABLE \`transaction\` (

&#x20; \`id\` bigint PRIMARY KEY AUTO\_INCREMENT,

&#x20; \`user\_id\` bigint NOT NULL,

&#x20; \`account\_id\` bigint NOT NULL,

&#x20; \`type\` varchar(10) NOT NULL, -- IN/OUT 收入/支出

&#x20; \`amount\` decimal(10,2) NOT NULL,

&#x20; \`category\` varchar(50),

&#x20; \`description\` varchar(200),

&#x20; \`trade\_time\` datetime,

&#x20; \`create\_time\` datetime

);
```

### 阶段三：核心业务服务开发（预计 3-4 天）

**推荐开发顺序**：`account → transaction → budget → report → invest`

（先实现单体 CRUD 功能，后续再集成微服务调用能力）

#### 1. Account 账户服务



* 实现账户的增删改查接口

* 预留 Feign 接口，供 Transaction 服务调用实现**账户余额更新**

#### 2. Transaction 收支服务



* 实现收支记录的增删改查核心功能

* 关键逻辑：创建收支记录时，通过 Feign 同步调用 Account 服务更新对应账户余额（后续可改为 MQ 异步实现最终一致性）

#### 3. Budget 预算服务



* 实现月度预算的创建、查询功能

* 调用 Transaction 服务统计当月分类支出，自动计算预算剩余额度

#### 4. Report 报表服务



* 聚合 Transaction 和 Budget 服务数据

* 实现收支总览、分类占比、预算执行率、收支趋势等统计报表功能

#### 5. Invest 投资服务



* 实现投资持仓的增删改查

* 支持手动录入当前价格，自动计算持仓收益（暂不接入第三方行情接口）

### 阶段四：Spring Cloud Alibaba 微服务集成（预计 2 天）

#### 1. Nacos 集成



* 所有服务添加 Nacos 依赖，配置 `spring.cloud``.nacos.discovery.server-addr` 实现服务注册

* 将数据库、Redis、MQ 等配置迁移至 Nacos 配置中心，实现配置动态刷新

#### 2. OpenFeign 服务间调用



* 按业务依赖定义 Feign 客户端，示例：


  * Transaction 服务中定义`AccountFeignClient`

  * Budget 服务中定义`TransactionFeignClient`

  * Report 服务中定义`TransactionFeignClient`和`BudgetFeignClient`

* 配置 Feign 超时时间、重试机制，集成 Sentinel 实现服务熔断

#### 3. Gateway 网关集成



* 配置路由规则，将`/api/**`请求转发至对应微服务

* 开发全局过滤器，实现 JWT Token 校验（排除`/api/auth/**`接口）

* 配置跨域规则，解决前端跨域问题

### 阶段五：分布式中间件集成（预计 2 天）

#### 1. Redis 分布式缓存集成



* 缓存场景：用户信息、账户余额、月度报表、预算信息

* 使用`@Cacheable`注解实现缓存，统一设置过期时间（建议 5 分钟）

* 缓存防护：实现空值缓存解决**缓存穿透**，分布式锁解决**缓存击穿**

#### 2. RabbitMQ 异步解耦集成

##### 定义 3 个核心消息队列



| 队列名称                     | 用途                                      |
| ------------------------ | --------------------------------------- |
| `budget_warn_queue`      | 预算超支时异步发送通知（日志 / 邮件 / 短信，基础版先实现日志通知）    |
| `transaction_sync_queue` | 收支记录创建后，异步同步报表数据、更新账户余额（替代原同步 Feign 调用） |
| `ai_classify_queue`      | AI 智能分类任务异步处理，处理结果回调写入 Transaction 服务   |

##### 消息生产与消费



* 在 Transaction/Budget 服务中实现**消息生产者**，发布业务事件

* 在 Account/Report/AI 服务中实现**消息消费者**，消费事件并处理业务

* 配置手动 ACK 确认，添加死信队列处理消费失败的消息

### 阶段六：高级特性开发（预计 2 天）

#### 1. 服务监控体系搭建



* 所有服务引入`spring-boot-starter-actuator`和`micrometer-registry-prometheus`依赖

* 暴露`/actuator/prometheus`监控端点，供 Prometheus 采集指标

* Prometheus 配置采集规则，Grafana 导入 JVM、接口耗时、服务调用等监控面板

#### 2. AI 智能记账功能开发



* 在 piggymini-ai 服务中集成 Spring AI，对接通义千问 / OpenAI 大模型

* 实现内部 Feign 接口`/internal/ai/classify`，完成文本解析与收支信息提取

* 业务流程：用户调用`/api/transactions/ai-classify` → 发送 MQ 消息 → AI 服务消费解析 → 结果写回 Transaction 服务

### 阶段七：云原生部署（预计 1-2 天）

#### 1. Docker 容器化部署



* 为每个微服务编写`Dockerfile`（基础镜像推荐 openjdk:17-jdk-slim）

* 编写`docker-compose.yml`文件，实现**所有服务 + 中间件**的一键启动

#### 2. Kubernetes 本地部署（Minikube）



* 为每个服务编写 K8s 资源文件：Deployment（部署）、Service（服务发现）、ConfigMap（配置）

* 将服务部署到 Minikube 本地集群，测试服务发现与负载均衡功能

### 阶段八：测试、优化与文档完善（预计 1 天）

#### 1. 接口测试与 Knife4j 文档集成



* 所有 Controller 添加 Knife4j 注解：类上`@Tag`，方法上`@Operation`

* 访问`http://localhost:8080/api/doc.html`进行全链路接口测试

#### 2. 核心功能检查清单



* JWT 鉴权：是否拦截未登录 / Token 失效的请求

* Feign 调用：是否配置超时、重试和熔断机制

* MQ 消费：是否开启手动 ACK + 死信队列

* Redis 缓存：缓存是否生效，是否做了穿透 / 击穿防护

* 监控指标：Prometheus 是否能正常采集到服务指标

#### 3. 项目扩展方向（可选开发）



* 定时任务：集成 ShedLock + `@Scheduled`，自动更新投资收益、生成月度报表

* 通知增强：实现邮件 / 企业微信的预算超支预警

* 数据导出：集成 EasyExcel，实现收支 / 报表数据的 Excel 导出

* 多用户扩展：实现 RBAC 权限模型，支持家庭多用户记账

## 五、项目启动与功能验证

### 1. 本地启动顺序

**第一步**：启动中间件

Nacos → MySQL → Redis → RabbitMQ

**第二步**：启动微服务（按依赖顺序）

`gateway` → `auth` → `account` → `transaction` → `budget` → `report` → `invest` → `ai`

**第三步**：访问接口文档

打开浏览器访问 `http://localhost:8080/api/doc.html`，进入 Knife4j 接口文档页面

### 2. 全链路功能测试流程



1. 调用`/api/auth/register`注册用户

2. 调用`/api/auth/login`登录，获取 JWT Token 并设置到请求头

3. 调用`/api/accounts`创建个人账户

4. 调用`/api/transactions`添加收支记录

5. 调用`/api/budgets`设置当月消费预算

6. 调用`/api/reports/**`查看各类统计报表

7. 调用`/api/transactions/ai-classify`测试 AI 智能分类记账

## 六、附录：Knife4j 集成示例代码



```
import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.\*;

@RestController

@RequestMapping("/api/accounts")

@Tag(name = "账户服务", description = "个人账户的增删改查接口")

public class AccountController {

&#x20;   @PostMapping

&#x20;   @Operation(summary = "新增账户", description = "创建用户的个人账户，支持现金/银行卡/支付宝类型")

&#x20;   public Result\<Account> create(@RequestBody AccountDto dto) {

&#x20;       // 业务逻辑实现

&#x20;       return Result.success(accountService.create(dto));

&#x20;   }

&#x20;   // 其他接口按此规范添加注解

}
```



***

**开发周期预估**：按此文档分步开发，预计 12–15 天可完成完整项目开发，项目具备**微服务架构**、**分布式中间件**、**云原生部署**和**AI 能力**，适合作为个人练手项目写入简历。

> （注：文档部分内容可能由 AI 生成）