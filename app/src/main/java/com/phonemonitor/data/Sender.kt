package com.phonemonitor.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.phonemonitor.log.LogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL
import java.util.Base64
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory

object Sender {
  private val piFlags = if (android.os.Build.VERSION.SDK_INT >= 23) android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT else android.app.PendingIntent.FLAG_UPDATE_CURRENT
  suspend fun sendAll(ctx: Context, rule: RuleConfig, msg: MsgData) {
    for (ch in rule.channels) {
      if (!ch.enabled) continue
      try {
        val target = when (ch.type) {
          SendType.EMAIL -> { sendEmail(ch, msg); ch.smtpTo }
          SendType.WEBHOOK -> { sendWebhook(ch, msg); ch.webhookUrl }
          SendType.URL -> { sendUrl(ctx, ch, msg); ch.urlScheme }
        }
        LogManager.ok("${ch.type.name} → $target 推送成功")
      } catch (e: Exception) {
        val target = when (ch.type) {
          SendType.EMAIL -> ch.smtpTo
          SendType.WEBHOOK -> ch.webhookUrl
          SendType.URL -> ch.urlScheme
        }
        LogManager.e("${ch.type.name}($target) 失败: ${e::class.simpleName}: ${e.message ?: "无详细信息"}")
      }
    }
  }

  private suspend fun sendEmail(cfg: ChannelConfig, msg: MsgData) = withContext(Dispatchers.IO) {
    val host = cfg.smtpHost
    val port = cfg.smtpPort
    val user = cfg.smtpUser
    val pass = cfg.smtpPass
    val from = cfg.smtpFrom
    val to = cfg.smtpTo
    val body = if (cfg.includeContent) msg.msgParam() else msg.title
    val subject = cfg.buildSubject(msg)

    val isSsl = port == 465
    val raw: Socket = if (isSsl) {
      SSLSocketFactory.getDefault().createSocket(host, port)
    } else {
      Socket(host, port)
    }
    raw.soTimeout = 20000

    var sock = raw
    var r = BufferedReader(InputStreamReader(sock.getInputStream(), "UTF-8"))
    var w = OutputStreamWriter(sock.getOutputStream(), "UTF-8")

    fun rl(): String { val l = r.readLine() ?: throw Exception("连接已断开"); return l }
    fun wl(s: String) { w.write("$s\r\n"); w.flush() }

    try {
      var resp = rl()
      if (!resp.startsWith("220")) throw Exception("服务器拒绝: $resp")

      wl("EHLO NotiMon"); resp = rl()
      while (resp.startsWith("250-")) resp = rl()
      if (!resp.startsWith("250")) throw Exception("EHLO 失败: $resp")

      if (!isSsl && port != 25) {
        wl("STARTTLS"); resp = rl()
        if (resp.startsWith("220")) {
          val ctx = javax.net.ssl.SSLContext.getInstance("TLS")
          ctx.init(null, null, null)
          val ssl = ctx.socketFactory.createSocket(sock, host, port, true) as Socket
          ssl.soTimeout = 20000
          sock.close()
          sock = ssl
          r = BufferedReader(InputStreamReader(sock.getInputStream(), "UTF-8"))
          w = OutputStreamWriter(sock.getOutputStream(), "UTF-8")
          wl("EHLO NotiMon"); resp = rl()
          while (resp.startsWith("250-")) resp = rl()
          if (!resp.startsWith("250")) throw Exception("EHLO 失败: $resp")
        }
      }

      wl("AUTH LOGIN"); resp = rl()
      if (!resp.startsWith("334")) throw Exception("不支持 AUTH: $resp")
      wl(Base64.getEncoder().encodeToString(user.toByteArray())); resp = rl()
      if (!resp.startsWith("334")) throw Exception("用户名被拒: $resp")
      wl(Base64.getEncoder().encodeToString(pass.toByteArray())); resp = rl()
      if (!resp.startsWith("235")) throw Exception("认证失败: $resp")

      wl("MAIL FROM:<$from>"); resp = rl()
      if (!resp.startsWith("250")) throw Exception("发件人被拒: $resp")

      wl("RCPT TO:<$to>"); resp = rl()
      if (!resp.startsWith("250")) throw Exception("收件人被拒: $resp")

      wl("DATA"); resp = rl()
      if (!resp.startsWith("354")) throw Exception("DATA 被拒: $resp")

      wl("From: $from")
      wl("To: $to")
      wl("Subject: =?UTF-8?B?${Base64.getEncoder().encodeToString(subject.toByteArray())}?=")
      wl("Content-Type: text/plain; charset=UTF-8")
      wl("")
      wl(body.replace("\r\n", "\n").replace("\n", "\r\n"))
      wl(".")

      resp = rl()
      if (!resp.startsWith("250")) throw Exception("发送被拒: $resp")

      wl("QUIT")
    } finally {
      try { sock.close() } catch (_: Exception) {}
    }
  }

  private suspend fun sendWebhook(cfg: ChannelConfig, msg: MsgData) = withContext(Dispatchers.IO) {
    val urlStr = cfg.webhookUrl.replace("[msg]", java.net.URLEncoder.encode(msg.msgParam(), "UTF-8"))
    val url = URL(urlStr)
    val conn = url.openConnection() as HttpURLConnection
    conn.connectTimeout = 10000
    conn.readTimeout = 10000
    conn.requestMethod = cfg.webhookMethod.uppercase()
    conn.doOutput = cfg.webhookMethod.uppercase() in listOf("POST", "PUT", "PATCH")
    conn.setRequestProperty("User-Agent", "NotiMon/1.0")
    if (conn.doOutput) {
      conn.setRequestProperty("Content-Type", "application/json")
      val json = """{"title":"${esc(msg.title)}","content":"${esc(msg.content)}","package":"${esc(msg.pkg)}"}"""
      OutputStreamWriter(conn.outputStream).use { it.write(json); it.flush() }
    }
    val code = conn.responseCode
    conn.disconnect()
    if (code != cfg.successCode) throw Exception("HTTP $code (期望 ${cfg.successCode})")
  }

  private fun sendUrl(ctx: Context, cfg: ChannelConfig, msg: MsgData) {
    val urlStr = cfg.urlScheme.replace("[msg]", java.net.URLEncoder.encode(msg.msgParam(), "UTF-8"))
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlStr)).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
    }
    val pi = android.app.PendingIntent.getActivity(ctx, 0, intent, piFlags)
    try {
      pi.send()
    } catch (_: Exception) {
      try {
        ctx.startActivity(intent)
      } catch (_: Exception) {
        val nm = ctx.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
          val ch = android.app.NotificationChannel("url_redirect", "URL 跳转", android.app.NotificationManager.IMPORTANCE_HIGH)
          nm.createNotificationChannel(ch)
        }
        val notif = androidx.core.app.NotificationCompat.Builder(ctx, "url_redirect")
          .setContentTitle("点击打开链接")
          .setContentText(urlStr)
          .setSmallIcon(android.R.drawable.ic_menu_compass)
          .setContentIntent(pi)
          .setAutoCancel(true)
          .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
          .build()
        nm.notify((System.currentTimeMillis() % 10000).toInt(), notif)
      }
    }
  }

  private fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
}
