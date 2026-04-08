# 认证模块（Auth）接口文档

**文档版本**：V1.0

**基础路径**：`/api/auth`

**数据格式**：请求 / 响应均为 JSON

**字符编码**：UTF-8

**统一返回格式**：所有接口均使用 `Result<T>` 标准化响应



***

## 一、统一响应结构

所有接口返回固定格式，方便前端统一处理：



```
{

&#x20; "code": 200,          // 状态码：200成功，4xx客户端错误，5xx服务端错误

&#x20; "message": "操作成功", // 响应描述信息

&#x20; "data": {},           // 业务数据，无数据时为null

&#x20; "success": true,      // 是否成功

&#x20; "timestamp": 1744100000000  // 响应时间戳

}
```



***

## 二、接口列表



1. 用户注册

2. 用户登录

3. 刷新令牌

4. 用户退出登录



***

## 1. 用户注册接口

### 基本信息



* **请求方式**：POST

* **接口地址**：`/api/auth/register`

* **接口描述**：创建新用户账号，用户名全局唯一

### 请求参数



| 参数名      | 类型     | 必填 | 说明    | 校验规则                 | 示例         |
| -------- | ------ | -- | ----- | -------------------- | ---------- |
| username | String | 是  | 登录用户名 | 4-20 位，字母 / 数字 / 下划线 | `test001`  |
| password | String | 是  | 登录密码  | 6-20 位，必须包含字母 + 数字   | `123456Ab` |
| nickname | String | 否  | 用户昵称  | 最大 20 字符             | `测试用户`     |

### 请求示例



```
{

&#x20; "username": "test001",

&#x20; "password": "123456Ab",

&#x20; "nickname": "测试用户"

}
```

### 响应示例（成功）



```
{

&#x20; "code": 200,

&#x20; "message": "注册成功",

&#x20; "data": {

&#x20;   "userId": 1001,

&#x20;   "username": "test001",

&#x20;   "nickname": "测试用户",

&#x20;   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",

&#x20;   "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

&#x20; },

&#x20; "success": true,

&#x20; "timestamp": 1744100000000

}
```

### 响应示例（失败）



```
{

&#x20; "code": 400,

&#x20; "message": "用户名已存在",

&#x20; "data": null,

&#x20; "success": false,

&#x20; "timestamp": 1744100000000

}
```



***

## 2. 用户登录接口

### 基本信息



* **请求方式**：POST

* **接口地址**：`/api/auth/login`

* **接口描述**：用户名密码验证登录，返回身份令牌

### 请求参数



| 参数名      | 类型     | 必填 | 说明  | 示例         |
| -------- | ------ | -- | --- | ---------- |
| username | String | 是  | 用户名 | `test001`  |
| password | String | 是  | 密码  | `123456Ab` |

### 请求示例



```
{

&#x20; "username": "test001",

&#x20; "password": "123456Ab"

}
```

### 响应示例（成功）



```
{

&#x20; "code": 200,

&#x20; "message": "登录成功",

&#x20; "data": {

&#x20;   "userId": 1001,

&#x20;   "username": "test001",

&#x20;   "nickname": "测试用户",

&#x20;   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",

&#x20;   "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

&#x20; },

&#x20; "success": true,

&#x20; "timestamp": 1744100000000

}
```

### 响应示例（失败）



```
{

&#x20; "code": 401,

&#x20; "message": "用户名或密码错误",

&#x20; "data": null,

&#x20; "success": false,

&#x20; "timestamp": 1744100000000

}
```



***

## 3. 刷新令牌接口

### 基本信息



* **请求方式**：POST

* **接口地址**：`/api/auth/refresh`

* **接口描述**：token 过期时，使用 refreshToken 获取新令牌

### 请求参数



| 参数名          | 类型     | 必填 | 说明   | 示例      |
| ------------ | ------ | -- | ---- | ------- |
| refreshToken | String | 是  | 刷新令牌 | 登录接口返回值 |

### 请求示例



```
{

&#x20; "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

}
```

### 响应示例（成功）



```
{

&#x20; "code": 200,

&#x20; "message": "刷新成功",

&#x20; "data": {

&#x20;   "userId": 1001,

&#x20;   "username": "test001",

&#x20;   "nickname": "测试用户",

&#x20;   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new",

&#x20;   "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new"

&#x20; },

&#x20; "success": true,

&#x20; "timestamp": 1744100000000

}
```

### 响应示例（失败）



```
{

&#x20; "code": 401,

&#x20; "message": "刷新令牌已失效，请重新登录",

&#x20; "data": null,

&#x20; "success": false,

&#x20; "timestamp": 1744100000000

}
```



***

## 4. 用户退出登录

### 基本信息



* **请求方式**：POST

* **接口地址**：`/api/auth/logout`

* **接口描述**：用户登出，失效当前令牌

* **请求头**：`Authorization: Bearer {token}`

### 请求示例

无请求体

### 响应示例



```
{

&#x20; "code": 200,

&#x20; "message": "退出登录成功",

&#x20; "data": null,

&#x20; "success": true,

&#x20; "timestamp": 1744100000000

}
```



***

## 三、通用错误码说明



| 错误码 | 含义         | 处理方案             |
| --- | ---------- | ---------------- |
| 200 | 请求成功       | 正常处理             |
| 400 | 请求参数错误     | 检查参数格式 / 长度 / 规则 |
| 401 | 未授权 / 令牌失效 | 重新登录或刷新 token    |
| 403 | 无权限访问      | 检查用户权限           |
| 500 | 服务器异常      | 联系后端开发人员         |



***

## 四、前端使用规范



1. **令牌携带**：登录后所有需要身份验证的接口，请求头添加：



```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```



1. **过期处理**：接口返回 401 时，优先调用刷新接口，刷新失败则跳转到登录页

2. **密码规则**：必须包含**字母 + 数字**，长度 6\~20 位



***

### 总结

这份文档是**生产可用的标准 RESTful 接口文档**，包含了完整的：



* 统一响应规范

* 4 个核心认证接口

* 请求 / 响应 / 失败示例

* 参数校验规则

* 错误码和前端使用规范

你可以直接复制到项目文档、Markdown 文件或接口管理工具中使用。

> （注：文档部分内容可能由 AI 生成）