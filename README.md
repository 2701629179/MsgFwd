# README\.md

# MsgFwd · 安卓消息转发器

一款简洁稳定、高度自定义的 Android 通知 \&amp; 短信监控转发工具，本地处理数据，安全无上传，支持多通道智能推送。

---

## 📌 功能介绍

### 1\. 消息监听

- **全局通知监听**：捕获手机内所有应用通知标题、内容、应用包名

- **短信实时监听**：自动拦截短信，解析发送人、短信正文、接收时间

### 2\. 智能过滤规则

- 支持全局监听 / 自定义指定应用监听

- 关键词包含匹配、全文完全匹配两种过滤模式

- 可自定义黑白名单，精准过滤无用消息

### 3\. 多通道推送

- **Webhook**：支持 POST / GET / PUT / PATCH 请求，标准 JSON 格式推送

- **SMTP 邮件推送**：兼容 SSL / TLS / STARTTLS 安全加密协议

- **URL 跳转**：支持自定义 URL Scheme，唤醒第三方应用

### 4\. 常驻保活能力

- 前台服务常驻运行，避免后台被杀

- AlarmManager 定时心跳保活机制

- WakeLock 唤醒锁，防止系统休眠终止进程

- 设备开机自启，重启自动恢复监听服务

### 5\. 便捷工具

- 规则配置剪贴板一键导入、导出

- 内置日志面板，实时查看推送记录与运行状态

---

## 🛠 技术栈

|类别|技术详情|
|---|---|
|开发语言|Kotlin 1\.9\.20|
|UI 框架|Jetpack Compose \+ Material3|
|项目架构|MVVM（ViewModel \+ StateFlow）|
|网络框架|OkHttp / HttpURLConnection|
|构建工具|Gradle 8\.5 \+ AGP 8\.2\.0|
|最低适配|Android 5\.0（API 21）|
|目标版本|Android 14（API 34）|

---

## 📲 下载使用

前往 [Releases](https://github.com/2701629179/MsgFwd/releases) 下载最新版本 APK

---

## 🔨 源码编译

支持 Windows / Mac / Linux 全平台编译

```Plain Text
git clone https://github.com/2701629179/MsgFwd.git
cd MsgFwd
./gradlew assembleRelease
```

编译产物输出目录：`app/build/outputs/apk/release/`

---

## 🔐 权限说明

|权限名称|使用用途|
|---|---|
|INTERNET|网络请求、Webhook、SMTP 推送服务|
|NOTIFICATION\_LISTENER|读取系统全局应用通知|
|RECEIVE\_SMS / READ\_SMS|接收、读取手机短信内容|
|FOREGROUND\_SERVICE|前台服务常驻，保障后台运行|
|RECEIVE\_BOOT\_COMPLETED|监听开机广播，实现开机自启|
|POST\_NOTIFICATIONS|展示前台服务常驻通知|
|WAKE\_LOCK|锁定设备唤醒状态，防止进程休眠被杀|

---

## 🛡 隐私声明

本项目所有消息数据均在**本地设备处理运算**，不会自动上传至任何第三方服务器。推送通道、过滤规则均由用户自主配置，全程保障用户隐私安全。

---

## 📄 开源协议

Copyright © 2026 项目作者:任城第一深情
联系方式 2701629179@qq.com

本项目基于 **GNU GPL v3\.0** 协议开源，仅供学习交流使用。

**免费开源 · 禁止倒卖 · 禁止二次售卖**



> （注：文档部分内容可能由 AI 生成）
