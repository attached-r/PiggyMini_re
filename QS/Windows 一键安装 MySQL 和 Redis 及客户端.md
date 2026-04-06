# Windows 一键安装：MySQL + Redis + 可视化客户端

全程**最简单、最稳、无坑版**，跟着点就能装好，我给你写得清清楚楚！



***

## 一、安装 MySQL 8.0（带客户端）

### 1. 下载 MySQL

直接去官网下**安装版**（最省心）：

👉 [MySQL Installer 8.0 下载](https://dev.mysql.com/downloads/installer/)

选 **MySQL Installer for Windows** → 下载第二个小的（\~20MB）

### 2. 安装步骤



1. 打开安装包 → 选 **Server Only**（只装服务端）

2. 一路下一步 → **端口保持 3306**

3. **设置密码**（一定要记住！比如：`root123456`）

4. 继续下一步直到完成

### 3. 安装 MySQL 客户端（Navicat）

这是最好用的可视化工具：

👉 [Navicat 16 下载](https://www.navicat.com/en/download/navicat-premium)

安装后：



* 新建连接 → MySQL

* 主机：`localhost`

* 端口：`3306`

* 用户名：`root`

* 密码：你刚才设置的

**点连接，成功就搞定！**



***

## 二、安装 Redis（带客户端）

### 1. 下载 Redis（Windows 专用版）

官方没有 Windows 版，用微软维护的稳定版：

👉 [Redis Windows 下载](https://github.com/tporadowski/redis/releases)

下 **Redis-x64-5.0.xx.zip**

### 2. 安装 / 启动



1. 解压到一个文件夹，比如 `D:\Redis`

2. 双击 `redis-server.exe` → 出现黑窗口 = 启动成功

3. **不要关这个窗口！关了 Redis 就停了**

### 3. 安装 Redis 客户端（Another Redis Desktop Manager）

最好用、免费、无广告：

👉 [Another Redis Desktop Manager 下载](https://github.com/qishibo/AnotherRedisDesktopManager/releases)

安装后打开：



* 新建连接

* 主机：`localhost`

* 端口：`6379`

* 密码：**默认没有，留空**

* 连接成功！



***

## 三、最关键：测试是否装好

### 测试 MySQL

打开 Navicat → 连接 MySQL → 能进 = 成功

### 测试 Redis

打开 Redis 客户端 → 连接 [localhost:6379](https://localhost:6379) → 能进 = 成功



***

# 超简总结（你只需要记这个）



1. **MySQL**：端口 3306，客户端 Navicat

2. **Redis**：端口 6379，客户端 Another Redis Desktop Manager

3. 两个都**默认本机&#x20;**[localhost](https://localhost)，直接连就能用

需要我给你发**Java 连接 MySQL + Redis 的现成代码**吗？直接复制就能用！

> （注：文档部分内容可能由 AI 生成）