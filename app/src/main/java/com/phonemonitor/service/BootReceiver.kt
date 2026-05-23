package com.phonemonitor.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.phonemonitor.log.LogManager

class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
      LogManager.i("手机开机，自动启动监控服务...")
      NotificationMonitorService.start(context)
    }
  }
}
