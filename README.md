📱 MsgFwd · 安卓消息转发器
一款轻量、稳定、高自定义的 Android 通知 / 短信监控转发工具
实时监听系统通知与短信内容，支持自定义过滤规则，多通道自动推送，支持后台保活、开机自启，纯本地处理、隐私安全。


---
✨ 项目特色
- 全量监听：全局应用通知 + 短信实时抓取
- 精准过滤：支持应用白名单、关键词匹配、完全匹配规则
- 多通道推送：Webhook / Email / URL 跳转
- 持久保活：前台服务 + 心跳机制 + 唤醒锁防杀
- 开机自启：设备重启自动恢复监听服务
- 配置互通：支持剪贴板快速导入/导出规则配置
- 日志可视化：内置日志面板，实时查看推送记录
- 隐私安全：所有数据本地运算，不上传第三方服务器

---
📌 核心功能
1. 消息监听
- 通知监听：抓取任意应用通知标题、内容、包名
- 短信监听：拦截短信，解析发送人、短信正文、时间
2. 过滤规则
- 全局监听 / 指定应用监听
- 关键词包含匹配、完全相等匹配
- 自定义黑白名单，精准过滤垃圾消息
3. 多渠道推送
- Webhook：支持 POST / GET / PUT / PATCH 标准 JSON 推送
- SMTP 邮件：支持 SSL / TLS / STARTTLS 安全发送
- URL 跳转：自定义 Scheme 链接唤醒其他应用
4. 稳定保活能力
- 前台服务常驻运行
- AlarmManager 定时心跳保活
- WakeLock 防止系统休眠查杀
- 开机广播自动重启服务

---
🛠 技术栈
分类
技术选型
开发语言
Kotlin 1.9.20
UI 框架
Jetpack Compose + Material3
项目架构
MVVM (ViewModel + StateFlow)
网络请求
OkHttp / HttpURLConnection
构建工具
Gradle 8.5 + AGP 8.2.0
最低适配
Android 5.0 (API 21)
目标版本
Android 14 (API 34)

---
📲 下载安装
可前往 Releases 下载最新正式版 APK

---
🔨 本地编译构建
支持 Windows / Mac / Linux 一键编译
git clone https://github.com/2701629179/MsgFwd.git
cd MsgFwd
./gradlew assembleRelease

编译输出路径：app/build/outputs/apk/release/

---
🔐 权限说明
权限名称
使用用途
INTERNET
网络请求、Webhook、SMTP 推送
NOTIFICATION_LISTENER
读取系统应用通知
RECEIVE_SMS / READ_SMS
接收与读取短信内容
FOREGROUND_SERVICE
前台服务保活运行
RECEIVE_BOOT_COMPLETED
开机自启、重启恢复服务
POST_NOTIFICATIONS
前台服务常驻通知展示
WAKE_LOCK
防止系统休眠杀进程

---
🛡 隐私声明
本项目所有消息数据仅在本地设备处理，不会自动上传任何第三方服务器。推送目标、过滤规则均由用户自行配置，绝对保障用户隐私安全。

---
📄 开源协议
Copyright © 2024 项目作者
本项目基于 GNU GPL v3.0 开源协议发布，开源免费、仅供学习使用，禁止私自倒卖、二次售卖。
免费开源 · 禁止倒卖 · 学习交流专用
