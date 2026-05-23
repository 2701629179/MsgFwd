# MsgFwd · 消息转发器

一款 Android 通知与短信监控转发工具。实时监听系统通知和短信，根据自定义规则将消息推送到指定通道。

## 功能

- **通知监听** — 监听所有应用的通知（标题 + 内容）
- **短信监听** — 拦截接收到的短信，提取发送者和内容
- **应用过滤** — 选择监听单个应用或所有应用
- **内容过滤** — 支持关键词匹配（包含 / 完全相等）
- **多通道推送**
  - **Email** — 通过 SMTP 发送（支持 SSL / TLS / STARTTLS）
  - **Webhook** — POST / GET / PUT / PATCH，JSON 格式
  - **URL 跳转** — 自定义 URL Scheme
- **开机自启** — 系统启动后自动运行
- **保活机制** — AlarmManager 心跳 + WakeLock 防杀
- **规则导入 / 导出** — 剪贴板导入导出配置
- **运行日志** — 内置日志面板，实时查看推送记录

## 下载

前往 [Releases](https://github.com/2701629179/MsgFwd/releases) 下载最新 APK。

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin 1.9.20 |
| UI | Jetpack Compose + Material3 |
| 架构 | MVVM (ViewModel + StateFlow) |
| 网络 | OkHttp + HttpURLConnection |
| 构建 | Gradle 8.5 + AGP 8.2.0 |
| 最低 SDK | Android 5.0 (API 21) |
| 目标 SDK | Android 14 (API 34) |

## 构建

```bash
git clone https://github.com/2701629179/MsgFwd.git
cd MsgFwd
./gradlew assembleRelease
```

构建产物位于 `app/build/outputs/apk/release/`。

## 权限说明

| 权限 | 用途 |
|------|------|
| `INTERNET` | Webhook / SMTP 网络请求 |
| `NOTIFICATION_LISTENER` | 监听通知 |
| `RECEIVE_SMS / READ_SMS` | 接收短信 |
| `FOREGROUND_SERVICE` | 前台服务保活 |
| `RECEIVE_BOOT_COMPLETED` | 开机自启 |
| `POST_NOTIFICATIONS` | 发送通知 |
| `WAKE_LOCK` | CPU 唤醒锁 |

## 隐私声明

本应用**不会**将你的通知和短信数据上传到第三方服务器。所有数据仅在本地处理，推送目标由用户自行配置。

## 许可证

Copyright © 2026 任城第一深情

本项目基于 [GNU General Public License v3.0](LICENSE) 发布。


