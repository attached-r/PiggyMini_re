完整的接口文档：

# Piggy 个人财务管理系统 - API 接口文档

## 基础信息

- **Base URL**: `http://localhost:8080` (Gateway 端口)
- **认证方式**: JWT Token
- **请求头**: 
  - `X-User-Id`: 用户ID（经过 Gateway 鉴权后自动注入）
  - `Authorization`: Bearer {token}（部分接口需要）
- **响应格式**: JSON

## 统一响应结构

```json
{
  "code": 200,
  "data": {},
  "message": "操作成功",
  "timestamp": 1712345678901
}
```


**状态码说明**:
- `200`: 成功
- `400`: 请求参数错误
- `401`: 未授权/Token 失效
- `403`: 禁止访问
- `404`: 资源不存在
- `500`: 服务器内部错误

---

## 一、认证模块 (Auth)

### 1.1 用户注册

**接口地址**: `POST /api/auth/register`

**请求体**:
```json
{
  "username": "testuser",
  "password": "pass1234",
  "nickname": "测试用户"
}
```


**字段说明**:
| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|----------|
| username | String | 是 | 4-20字符，仅字母数字下划线 |
| password | String | 是 | 6-20字符，必须包含字母和数字 |
| nickname | String | 否 | 最多20字符 |

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "userId": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "message": "注册成功",
  "timestamp": 1712345678901
}
```


---

### 1.2 用户登录

**接口地址**: `POST /api/auth/login`

**请求体**:
```json
{
  "username": "testuser",
  "password": "pass1234"
}
```


**字段说明**:
| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|----------|
| username | String | 是 | 4-20字符 |
| password | String | 是 | 6-20字符 |

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "userId": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "message": "登录成功",
  "timestamp": 1712345678901
}
```


---

### 1.3 刷新 Token

**接口地址**: `POST /api/auth/refresh`

**请求体**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```


**响应示例**:
```json
{
  "code": 200,
  "data": {
    "userId": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "message": "Token 刷新成功",
  "timestamp": 1712345678901
}
```


---

## 二、账户模块 (Account)

### 2.1 创建账户

**接口地址**: `POST /api/accounts`

**请求头**: `X-User-Id: 1`

**请求体**:
```json
{
  "accountName": "工商银行储蓄卡",
  "accountType": "DEBIT_CARD",
  "balance": 10000.00,
  "currency": "CNY",
  "icon": "💳",
  "remark": "工资卡",
  "sortOrder": 1,
  "isDefault": 1
}
```


**字段说明**:
| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| accountName | String | 是 | 最多50字符 | 账户名称 |
| accountType | Enum | 是 | 见枚举说明 | 账户类型 |
| balance | BigDecimal | 否 | ≥0.00 | 初始余额，默认0 |
| currency | String | 否 | 最多10字符 | 币种，默认CNY |
| icon | String | 否 | 最多50字符 | 账户图标 |
| remark | String | 否 | 最多200字符 | 备注 |
| sortOrder | Integer | 否 | - | 排序顺序 |
| isDefault | Integer | 否 | 0或1 | 是否默认账户 |

**账户类型枚举 (AccountType)**:
- `CASH`: 现金 💵
- `DEBIT_CARD`: 储蓄卡 💳
- `CREDIT_CARD`: 信用卡 💎
- `ALIPAY`: 支付宝 🔵
- `WECHAT`: 微信 💚
- `BANK_TRANSFER`: 银行转账 🏦
- `INVESTMENT`: 投资账户 📈
- `VIRTUAL`: 虚拟账户 🎮
- `OTHER`: 其他 📦

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "userId": 1,
    "accountName": "工商银行储蓄卡",
    "accountType": "DEBIT_CARD",
    "balance": 10000.00,
    "currency": "CNY",
    "icon": "💳",
    "remark": "工资卡",
    "sortOrder": 1,
    "isDefault": 1,
    "createTime": "2026-04-13T10:00:00",
    "updateTime": "2026-04-13T10:00:00"
  },
  "message": "新增账户成功",
  "timestamp": 1712345678901
}
```


---

### 2.2 查询账户列表

**接口地址**: `GET /api/accounts?page=1&size=10`

**请求头**: `X-User-Id: 1`

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "accountName": "工商银行储蓄卡",
        "accountType": "DEBIT_CARD",
        "balance": 10000.00,
        "currency": "CNY",
        "icon": "💳",
        "remark": "工资卡",
        "sortOrder": 1,
        "isDefault": 1,
        "createTime": "2026-04-13T10:00:00",
        "updateTime": "2026-04-13T10:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "message": "查询成功",
  "timestamp": 1712345678901
}
```


---

### 2.3 查询账户详情

**接口地址**: `GET /api/accounts/{id}`

**请求头**: `X-User-Id: 1`

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 账户ID |

**响应示例**: 同创建账户响应

---

### 2.4 更新账户

**接口地址**: `PUT /api/accounts/{id}`

**请求头**: `X-User-Id: 1`

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 账户ID |

**请求体** (所有字段可选):
```json
{
  "accountName": "工商银行储蓄卡(新)",
  "accountType": "DEBIT_CARD",
  "balance": 15000.00,
  "currency": "CNY",
  "icon": "💳",
  "remark": "主要工资卡",
  "sortOrder": 1,
  "isDefault": 1
}
```


**响应示例**: 同创建账户响应

---

### 2.5 删除账户

**接口地址**: `DELETE /api/accounts/{id}`

**请求头**: `X-User-Id: 1`

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 账户ID |

**响应示例**:
```json
{
  "code": 200,
  "data": null,
  "message": "删除账户成功",
  "timestamp": 1712345678901
}
```


---

## 三、交易模块 (Transaction)

### 3.1 创建交易记录

**接口地址**: `POST /api/transactions`

**请求头**: `X-User-Id: 1`

**请求体**:
```json
{
  "accountId": 1,
  "type": "EXPENSE",
  "amount": 50.00,
  "category": "FOOD_LUNCH",
  "description": "午餐",
  "tradeTime": "2026-04-13T12:00:00",
  "remark": "公司附近餐厅",
  "tags": "工作餐,外卖",
  "targetAccountId": null
}
```


**字段说明**:
| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| accountId | Long | 是 | - | 账户ID |
| type | Enum | 是 | EXPENSE/INCOME/TRANSFER | 交易类型 |
| amount | BigDecimal | 是 | >0.01 | 交易金额 |
| category | Enum | 否 | 见消费分类枚举 | 消费分类 |
| description | String | 否 | - | 交易描述 |
| tradeTime | LocalDateTime | 是 | - | 交易时间 |
| remark | String | 否 | 最多200字符 | 备注 |
| tags | String | 否 | 最多200字符 | 标签（逗号分隔） |
| targetAccountId | Long | 否 | - | 转账目标账户（仅转账类型） |

**交易类型枚举 (TransactionType)**:
- `EXPENSE`: 支出 💸
- `INCOME`: 收入 💰
- `TRANSFER`: 转账 🔄

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "userId": 1,
    "accountId": 1,
    "transactionType": "EXPENSE",
    "amount": 50.00,
    "category": "FOOD_LUNCH",
    "transactionTime": "2026-04-13T12:00:00",
    "remark": "公司附近餐厅",
    "tags": "工作餐,外卖",
    "targetAccountId": null,
    "createTime": "2026-04-13T12:00:00",
    "updateTime": "2026-04-13T12:00:00"
  },
  "message": "新增交易记录成功",
  "timestamp": 1712345678901
}
```


---

### 3.2 查询交易列表

**接口地址**: `GET /api/transactions`

**请求头**: `X-User-Id: 1`

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| accountId | Long | 否 | 账户ID筛选 |
| type | String | 否 | 交易类型筛选 (EXPENSE/INCOME/TRANSFER) |
| startDate | LocalDateTime | 否 | 开始时间 |
| endDate | LocalDateTime | 否 | 结束时间 |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "accountId": 1,
        "transactionType": "EXPENSE",
        "amount": 50.00,
        "category": "FOOD_LUNCH",
        "transactionTime": "2026-04-13T12:00:00",
        "remark": "公司附近餐厅",
        "tags": "工作餐,外卖",
        "targetAccountId": null,
        "createTime": "2026-04-13T12:00:00",
        "updateTime": "2026-04-13T12:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "message": "查询成功",
  "timestamp": 1712345678901
}
```


---

### 3.3 查询交易详情

**接口地址**: `GET /api/transactions/{id}`

**请求头**: `X-User-Id: 1`

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 交易ID |

**响应示例**: 同创建交易响应

---

### 3.4 更新交易记录

**接口地址**: `PUT /api/transactions/{id}`

**请求头**: `X-User-Id: 1`

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 交易ID |

**请求体** (所有字段可选):
```json
{
  "accountId": 1,
  "type": "EXPENSE",
  "amount": 55.00,
  "category": "FOOD_LUNCH",
  "description": "午餐（修改）",
  "tradeTime": "2026-04-13T12:00:00",
  "remark": "公司附近餐厅",
  "tags": "工作餐",
  "targetAccountId": null
}
```


**响应示例**: 同创建交易响应

---

### 3.5 删除交易记录

**接口地址**: `DELETE /api/transactions/{id}`

**请求头**: `X-User-Id: 1`

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 交易ID |

**响应示例**:
```json
{
  "code": 200,
  "data": null,
  "message": "删除交易记录成功",
  "timestamp": 1712345678901
}
```


---

### 3.6 AI 智能分类

**接口地址**: `POST /api/transactions/ai-classify`

**请求体**:
```json
{
  "text": "在公司楼下吃了个麦当劳套餐"
}
```


**字段说明**:
| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|----------|
| text | String | 是 | 最多500字符 |

**响应示例**:
```json
{
  "code": 200,
  "data": "FOOD_LUNCH",
  "message": "AI分类成功",
  "timestamp": 1712345678901
}
```


---

## 四、预算模块 (Budget)

### 4.1 创建/更新预算

**接口地址**: `POST /api/budgets`

**请求头**: `X-User-Id: 1`

**请求体**:
```json
{
  "month": "2026-04",
  "category": "FOOD",
  "amount": 2000.00
}
```


**字段说明**:
| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| month | String | 是 | yyyy-MM格式 | 预算月份 |
| category | Enum | 否 | 消费分类枚举 | 分类预算（为空表示总预算） |
| amount | BigDecimal | 是 | >0.01 | 预算金额 |

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "userId": 1,
    "budgetAmount": 2000.00,
    "usedAmount": 500.00,
    "cycleType": 2,
    "category": "FOOD_LUNCH",
    "startTime": "2026-04-01T00:00:00",
    "endTime": "2026-04-30T23:59:59",
    "status": 1,
    "warningThreshold": 80,
    "createTime": "2026-04-13T10:00:00",
    "updateTime": "2026-04-13T10:00:00"
  },
  "message": "预算设置成功",
  "timestamp": 1712345678901
}
```


---

### 4.2 查询当月预算

**接口地址**: `GET /api/budgets/current`

**请求头**: `X-User-Id: 1`

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "category": "FOOD_LUNCH",
      "budget": 2000.00,
      "spent": 500.00,
      "remain": 1500.00
    },
    {
      "category": null,
      "budget": 5000.00,
      "spent": 2000.00,
      "remain": 3000.00
    }
  ],
  "message": "查询成功",
  "timestamp": 1712345678901
}
```


---

### 4.3 超支预警列表

**接口地址**: `GET /api/budgets/warn`

**请求头**: `X-User-Id: 1`

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "category": "ENTERTAIN_MOVIE",
      "budget": 500.00,
      "spent": 450.00,
      "remain": 50.00
    }
  ],
  "message": "查询成功",
  "timestamp": 1712345678901
}
```


---

## 五、报表模块 (Report)

### 5.1 获取报表摘要

**接口地址**: `GET /api/reports/summary?period=month&date=2026-04`

**请求头**: `X-User-Id: 1`

**查询参数**:
| 参数 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| period | String | 是 | month/year | 时间周期 |
| date | String | 是 | yyyy-MM格式 | 查询日期 |

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "income": 8000.00,
    "expense": 3500.00,
    "balance": 4500.00,
    "trend": "up"
  },
  "message": "success",
  "timestamp": 1712345678901
}
```


---

### 5.2 获取分类报表

**接口地址**: `GET /api/reports/category?period=month&date=2026-04`

**请求头**: `X-User-Id: 1`

**查询参数**:
| 参数 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| period | String | 是 | month/year | 时间周期 |
| date | String | 是 | yyyy-MM格式 | 查询日期 |

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "category": "餐饮",
      "amount": 1200.00,
      "percent": 34.29
    },
    {
      "category": "交通",
      "amount": 500.00,
      "percent": 14.29
    },
    {
      "category": "娱乐",
      "amount": 800.00,
      "percent": 22.86
    }
  ],
  "message": "success",
  "timestamp": 1712345678901
}
```


---

### 5.3 获取预算执行率报表

**接口地址**: `GET /api/reports/budget-execution?month=2026-04`

**请求头**: `X-User-Id: 1`

**查询参数**:
| 参数 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| month | String | 是 | yyyy-MM格式 | 目标月份 |

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "category": "餐饮",
      "budgetAmount": 2000.00,
      "usedAmount": 1200.00,
      "remainingAmount": 800.00,
      "executionRate": 60.0,
      "isOverBudget": false
    },
    {
      "category": "娱乐",
      "budgetAmount": 500.00,
      "usedAmount": 550.00,
      "remainingAmount": -50.00,
      "executionRate": 110.0,
      "isOverBudget": true
    }
  ],
  "message": "success",
  "timestamp": 1712345678901
}
```


---

### 5.4 获取收支趋势报表

**接口地址**: `GET /api/reports/trend?startDate=2026-04-01&endDate=2026-04-30`

**请求头**: `X-User-Id: 1`

**查询参数**:
| 参数 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| startDate | String | 是 | yyyy-MM-dd格式 | 开始日期 |
| endDate | String | 是 | yyyy-MM-dd格式 | 结束日期 |

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "dates": ["2026-04-01", "2026-04-02", "2026-04-03", "..."],
    "income": [500.00, 0.00, 800.00, "..."],
    "expense": [200.00, 150.00, 300.00, "..."]
  },
  "message": "success",
  "timestamp": 1712345678901
}
```


---

## 六、消费分类枚举参考

### 餐饮类 (100-199)
- `FOOD_BREAKFAST`: 早餐 🥐
- `FOOD_LUNCH`: 午餐 🍱
- `FOOD_DINNER`: 晚餐 🍜
- `FOOD_SNACK`: 零食 🍿
- `FOOD_FRUIT`: 水果 🍎
- `FOOD_DRINK`: 饮品 🥤
- `FOOD_GROCERY`: 买菜 🥬
- `FOOD_TREAT`: 聚餐 🍻
- `FOOD_DELIVERY`: 外卖 🛵

### 交通类 (200-299)
- `TRANSPORT_SUBWAY`: 地铁 🚇
- `TRANSPORT_BUS`: 公交 🚌
- `TRANSPORT_TAXI`: 打车 🚕
- `TRANSPORT_DIDI`: 网约车 🚗
- `TRANSPORT_FUEL`: 加油 ⛽
- `TRANSPORT_PARKING`: 停车费 🅿️
- `TRANSPORT_TOLL`: 过路费 🛣️
- `TRANSPORT_BIKE`: 共享单车 🚲
- `TRANSPORT_TRAIN`: 火车/高铁 🚄
- `TRANSPORT_FLIGHT`: 机票 ✈️

### 购物类 (300-399)
- `SHOP_CLOTHES`: 服饰 👔
- `SHOP_SHOES`: 鞋靴 👟
- `SHOP_BAG`: 箱包 👜
- `SHOP_COSMETIC`: 美妆 💄
- `SHOP_DIGITAL`: 数码 💻
- `SHOP_HOME`: 家居 🏠
- `SHOP_DAILY`: 日用品 🧴
- `SHOP_BOOK`: 书籍 📚
- `SHOP_GIFT`: 礼物 🎁

### 居住类 (400-499)
- `LIVING_RENT`: 房租 🏢
- `LIVING_MORTGAGE`: 房贷 🏦
- `LIVING_PROPERTY`: 物业费 🔑
- `LIVING_WATER`: 水费 💧
- `LIVING_ELECTRIC`: 电费 ⚡
- `LIVING_GAS`: 燃气费 🔥
- `LIVING_INTERNET`: 网费 🌐
- `LIVING_MAINTENANCE`: 维修 🔧

### 娱乐类 (500-599)
- `ENTERTAIN_MOVIE`: 电影 🎬
- `ENTERTAIN_GAME`: 游戏 🎮
- `ENTERTAIN_KTV`: KTV 🎤
- `ENTERTAIN_TRAVEL`: 旅游 🏖️
- `ENTERTAIN_SPORT`: 运动 ⚽
- `ENTERTAIN_GYM`: 健身 🏋️
- `ENTERTAIN_STREAM`: 会员订阅 📺

### 医疗类 (600-699)
- `MEDICAL_HOSPITAL`: 看病 🏥
- `MEDICAL_MEDICINE`: 买药 💊
- `MEDICAL_DENTAL`: 牙科 🦷
- `MEDICAL_CHECKUP`: 体检 🩺
- `MEDICAL_INSURANCE`: 保险 🛡️

### 教育类 (700-799)
- `EDUCATION_TUITION`: 学费 🎓
- `EDUCATION_COURSE`: 培训 📖
- `EDUCATION_EXAM`: 考试 📝
- `EDUCATION_MATERIAL`: 教材 📚

### 社交类 (800-899)
- `SOCIAL_RED_PACKET`: 红包 🧧
- `SOCIAL_DONATION`: 捐赠 ❤️
- `SOCIAL_TIP`: 小费 💰

### 其他类 (900-999)
- `OTHER_MISC`: 杂项 📦
- `OTHER_LOSS`: 损失 💸
- `OTHER_PENALTY`: 罚款 ⚠️

---

## 七、错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权/Token 失效 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 409 | 资源冲突（如用户名已存在） |
| 500 | 服务器内部错误 |

---

## 八、注意事项

1. **认证机制**: 除注册、登录、刷新 Token 外，所有接口都需要通过 Gateway 鉴权，Gateway 会自动在请求头中注入 `X-User-Id`。

2. **事务一致性**: 创建/删除交易记录时，系统会自动更新对应账户的余额。

3. **预算预警**: 当某分类的实际支出达到预算的预警阈值（默认80%）时，会在预警列表中显示。

4. **分页查询**: 所有列表接口均支持分页，使用 MyBatis-Plus 的分页插件。

5. **时间格式**: 
   - LocalDateTime: `yyyy-MM-ddTHH:mm:ss`
   - LocalDate: `yyyy-MM-dd`
   - 年月: `yyyy-MM`

6. **内部 RPC 接口**: 以下接口仅供微服务间调用，不对外暴露：
   - `POST /api/accounts/balance`: 更新账户余额
   - `GET /api/transactions/statistics/category-expense`: 按分类统计支出
   - `GET /api/transactions/statistics/category-income`: 按分类统计收入

---

**文档版本**: v1.0  
**最后更新**: 2026-04-13  
**技术栈**: Spring Boot 4 + Java 17 + MyBatis-Plus + Redis + JWT