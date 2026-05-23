package com.phonemonitor.monitor

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppInfo(
  val packageName: String,
  val appName: String,
  val icon: Drawable? = null,
  val isSystem: Boolean = false
)

object AppLister {
  suspend fun getAllApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
    val pm = context.packageManager
    val list = mutableListOf<AppInfo>()
    try {
      val apps = pm.getInstalledApplications(PackageManager.MATCH_ALL)
      for (app in apps) {
        val name = try { pm.getApplicationLabel(app).toString() } catch (_: Exception) { app.packageName }
        val icon = try { app.loadIcon(pm) } catch (_: Exception) { null }
        val isSys = (app.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
        if (pm.getLaunchIntentForPackage(app.packageName) != null || !isSys) {
          list.add(AppInfo(app.packageName, name, icon, isSys))
        }
      }
    } catch (_: Exception) {}
    list.sortedBy { it.appName }
  }
}
