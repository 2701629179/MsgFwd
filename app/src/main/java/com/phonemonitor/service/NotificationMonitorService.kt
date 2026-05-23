package com.phonemonitor.service

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telephony.SmsMessage
import androidx.core.app.NotificationCompat
import com.phonemonitor.log.LogManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NotificationMonitorService : NotificationListenerService() {
  companion object {
    var isRunning = false; private set
    var onNotification: ((String, String, String) -> Unit)? = null
    private var notifJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private const val CHANNEL_ID = "monitor_channel"
    private val piFlags = if (android.os.Build.VERSION.SDK_INT >= 23) android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT else android.app.PendingIntent.FLAG_UPDATE_CURRENT

    fun start(c: Context) {
      val i = Intent(c, NotificationMonitorService::class.java)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) c.startForegroundService(i) else c.startService(i)
      startHeartbeat(c)
    }
    fun stop(c: Context) {
      stopHeartbeat(c)
      c.stopService(Intent(c, NotificationMonitorService::class.java))
    }

    private fun startHeartbeat(c: Context) {
      val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
      val pi = PendingIntent.getBroadcast(c, 0, Intent(c, KeepAliveReceiver::class.java), piFlags)
      am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 30000, 30000, pi)
    }
    private fun stopHeartbeat(c: Context) {
      val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
      val pi = PendingIntent.getBroadcast(c, 0, Intent(c, KeepAliveReceiver::class.java), piFlags)
      am.cancel(pi)
    }
  }

  private val scope = CoroutineScope(Dispatchers.IO)
  private val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.CHINA)
  private val smsRcv = object : BroadcastReceiver() {
    override fun onReceive(c: Context?, i: Intent?) {
      if (i?.action != "android.provider.Telephony.SMS_RECEIVED") return
      val bundle = i.extras ?: return
      try {
        for (pdu in (bundle.get("pdus") as? Array<*>) ?: return) {
          val msg = SmsMessage.createFromPdu(pdu as ByteArray)
          val sender = msg.displayOriginatingAddress ?: "未知"; val body = msg.messageBody ?: ""
          onNotification?.invoke(sender, body, "sms")
        }
      } catch (_: Exception) {}
    }
  }

  override fun onCreate() {
    super.onCreate()
    isRunning = true
    acquireWakeLock()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val c = android.app.NotificationChannel(CHANNEL_ID, "通知监控", android.app.NotificationManager.IMPORTANCE_LOW)
      c.setShowBadge(false); (getSystemService(android.app.NotificationManager::class.java)).createNotificationChannel(c)
    }
    updateNotif()
    startNotifUpdater()
    registerReceiver(smsRcv, IntentFilter("android.provider.Telephony.SMS_RECEIVED").apply { priority = Int.MAX_VALUE })
    LogManager.i("服务已启动，正在后台监控...")
  }

  override fun onStartCommand(i: Intent?, f: Int, s: Int): Int {
    updateNotif(); startNotifUpdater(); return START_STICKY
  }

  override fun onDestroy() {
    super.onDestroy(); isRunning = false; notifJob?.cancel()
    releaseWakeLock()
    try { unregisterReceiver(smsRcv) } catch (_: Exception) {}
    LogManager.w("服务已停止")
  }

  override fun onListenerConnected() { LogManager.ok("通知监听已授权") }
  override fun onListenerDisconnected() { LogManager.w("通知监听已断开") }

  override fun onNotificationPosted(sbn: StatusBarNotification) {
    val ex = sbn.notification.extras
    val t = ex.getString(Notification.EXTRA_TITLE, "") ?: ""
    val c = ex.getString(Notification.EXTRA_TEXT, "") ?: ""
    val p = sbn.packageName
    if (t.isBlank() && c.isBlank()) return
    onNotification?.invoke(t, c, p)
  }

  override fun onNotificationRemoved(sbn: StatusBarNotification?) {}

  private fun acquireWakeLock() {
    try {
      val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
      wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NotiMon:KeepAlive")
      wakeLock?.acquire(30 * 60 * 1000L)
    } catch (_: Exception) {}
  }

  private fun releaseWakeLock() {
    try { if (wakeLock?.isHeld == true) wakeLock?.release() } catch (_: Exception) {}
    wakeLock = null
  }

  private fun updateNotif() {
    val now = timeFmt.format(Date())
    startForeground(1, NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("通知监控 · 运行中")
      .setContentText("正在监听中 $now")
      .setSmallIcon(android.R.drawable.ic_menu_compass)
      .setOngoing(true)
      .setShowWhen(true)
      .setUsesChronometer(true)
      .setSilent(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .build())
  }

  private fun startNotifUpdater() {
    notifJob?.cancel()
    notifJob = scope.launch {
      while (true) {
        delay(1000)
        updateNotif()
      }
    }
  }
}