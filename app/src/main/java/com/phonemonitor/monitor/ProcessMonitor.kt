package com.phonemonitor.monitor

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ProcessInfo(val packageName: String, val appName: String, val pid: Int = 0, val isRunning: Boolean = true)

object ProcessMonitor {
  suspend fun getAllApps(context: Context): List<ProcessInfo> = withContext(Dispatchers.IO) {
    val pm = context.packageManager
    val list = mutableListOf<ProcessInfo>()
    try {
      val apps = pm.getInstalledApplications(PackageManager.MATCH_ALL)
      for (app in apps) {
        val name = try { pm.getApplicationLabel(app).toString() } catch (_: Exception) { app.packageName }
        try {
          if (pm.getLaunchIntentForPackage(app.packageName) != null || app.icon != 0) {
            list.add(ProcessInfo(app.packageName, name))
          }
        } catch (_: Exception) {
          list.add(ProcessInfo(app.packageName, name))
        }
      }
    } catch (_: Exception) {}
    list.sortedBy { it.appName }
  }
}
