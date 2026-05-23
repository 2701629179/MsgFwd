package com.phonemonitor.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class KeepAliveReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    if (!NotificationMonitorService.isRunning) {
      val i = Intent(context, NotificationMonitorService::class.java)
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        context.startForegroundService(i)
      } else {
        context.startService(i)
      }
    }
  }
}