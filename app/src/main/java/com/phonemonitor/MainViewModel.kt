package com.phonemonitor

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phonemonitor.data.ChannelConfig
import com.phonemonitor.data.MsgData
import com.phonemonitor.data.RuleConfig
import com.phonemonitor.data.SendType
import com.phonemonitor.data.Sender
import com.phonemonitor.data.SettingsRepo
import com.phonemonitor.log.LogManager
import com.phonemonitor.monitor.AppInfo
import com.phonemonitor.monitor.AppLister
import com.phonemonitor.service.NotificationMonitorService
import com.phonemonitor.ui.components.LogEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
  val logs: StateFlow<List<LogEntry>> = LogManager.logs
  private val repo = SettingsRepo(application)

  private val _running = MutableStateFlow(false); val running: StateFlow<Boolean> = _running.asStateFlow()
  private val _rule = MutableStateFlow(RuleConfig()); val rule: StateFlow<RuleConfig> = _rule.asStateFlow()
  private val _apps = MutableStateFlow<List<AppInfo>>(emptyList()); val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()
  private val _filteredApps = MutableStateFlow<List<AppInfo>>(emptyList()); val filteredApps: StateFlow<List<AppInfo>> = _filteredApps.asStateFlow()
  private val _showApps = MutableStateFlow(false); val showApps: StateFlow<Boolean> = _showApps.asStateFlow()
  private val _showSys = MutableStateFlow(true); val showSys: StateFlow<Boolean> = _showSys.asStateFlow()
  private val _appSearch = MutableStateFlow(""); val appSearch: StateFlow<String> = _appSearch.asStateFlow()
  private val _saveTs = MutableStateFlow(0L); val saveTs: StateFlow<Long> = _saveTs.asStateFlow()
  private val _toggleTs = MutableStateFlow(0L); val toggleTs: StateFlow<Long> = _toggleTs.asStateFlow()
  private val _toggleMsg = MutableStateFlow(""); val toggleMsg: StateFlow<String> = _toggleMsg.asStateFlow()
  private var matchCnt = 0

  init {
    _rule.value = repo.loadRule()
    NotificationMonitorService.onNotification = { t, c, p -> onNotify(t, c, p) }
  }

  fun toggle() {
    val ctx = getApplication<Application>()
    if (_running.value) { stop(ctx); showToggleMsg("关闭成功") }
    else { start(ctx); showToggleMsg("开启成功") }
  }

  private fun showToggleMsg(msg: String) {
    _toggleMsg.value = msg; _toggleTs.value = System.currentTimeMillis()
  }

  private fun start(ctx: Application) {
    if (!NotificationManagerCompat.getEnabledListenerPackages(ctx).contains(ctx.packageName)) {
      LogManager.w("需要通知监听权限"); ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); return
    }
    matchCnt = 0; NotificationMonitorService.start(ctx); _running.value = true
    LogManager.ok("══════ 启动监控 ══════"); LogManager.i("等待通知和短信...")
  }

  private fun stop(ctx: Application) {
    NotificationMonitorService.stop(ctx); _running.value = false
    LogManager.ok("══════ 已停止 (匹配 $matchCnt 次) ══════")
  }

  fun loadApps() = viewModelScope.launch {
    _apps.value = AppLister.getAllApps(getApplication())
    applyFilter()
  }

  fun setAppSearch(q: String) { _appSearch.value = q; applyFilter() }
  fun setShowSys(v: Boolean) { _showSys.value = v; applyFilter() }

  private fun applyFilter() {
    val q = _appSearch.value.lowercase(); val sys = _showSys.value
    _filteredApps.value = _apps.value.filter {
      (sys || !it.isSystem) && (q.isEmpty() || it.appName.lowercase().contains(q) || it.packageName.lowercase().contains(q))
    }
  }

  fun selectApp(a: AppInfo) {
    _rule.value = _rule.value.copy(selectedPkg = a.packageName, selectedAppName = a.appName)
    _showApps.value = false; LogManager.i("监听应用: ${a.appName}"); save()
  }

  fun showAppDialog() { loadApps(); _showApps.value = true }
  fun hideAppDialog() { _showApps.value = false }

  fun updateRule(r: RuleConfig) { _rule.value = r; save() }

  fun addChannel(type: SendType) {
    val cur = _rule.value
    _rule.value = cur.copy(channels = (cur.channels + ChannelConfig(type)).toMutableList())
    save()
  }

  fun removeChannel(idx: Int) {
    val cur = _rule.value
    val m = cur.channels.toMutableList()
    if (idx in m.indices) { m.removeAt(idx); _rule.value = cur.copy(channels = m); save() }
  }

  fun updateChannel(idx: Int, ch: ChannelConfig) {
    val cur = _rule.value
    val m = cur.channels.toMutableList()
    if (idx in m.indices) { m[idx] = ch; _rule.value = cur.copy(channels = m); save() }
  }

  private fun save() { repo.saveRule(_rule.value); _saveTs.value = System.currentTimeMillis() }

  fun exportRules(): String = repo.getExportJson()

  fun importRules(json: String) {
    try { repo.importFromJson(json); _rule.value = repo.loadRule(); LogManager.ok("规则导入成功") }
    catch (e: Exception) { LogManager.e("导入失败: ${e.message}") }
  }

  fun requestBatteryOpt() {
    val ctx = getApplication<Application>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      val pm = ctx.getSystemService(android.content.Context.POWER_SERVICE) as PowerManager
      if (!pm.isIgnoringBatteryOptimizations(ctx.packageName)) {
        ctx.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
          data = Uri.parse("package:${ctx.packageName}")
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        LogManager.i("已请求电池优化豁免")
      } else LogManager.i("已忽略电池优化")
    }
  }

  fun requestBackgroundPerm() {
    val ctx = getApplication<Application>()
    val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
      data = Uri.parse("package:${ctx.packageName}")
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    ctx.startActivity(i)
    LogManager.i("请在设置中允许后台运行")
  }

  private fun onNotify(title: String, content: String, pkg: String) {
    val r = _rule.value
    val appName = try {
      val ctx = getApplication<Application>(); val ai = ctx.packageManager.getApplicationInfo(pkg, 0)
      ctx.packageManager.getApplicationLabel(ai).toString()
    } catch (_: Exception) { pkg }

    if (r.selectedPkg.isNotEmpty() && pkg != r.selectedPkg) return
    if (r.titleEnabled && r.titleValue.isNotBlank()) {
      if (if (r.titleExact) title != r.titleValue else !title.contains(r.titleValue, true)) return
    }
    if (r.contentEnabled && r.contentValue.isNotBlank()) {
      if (if (r.contentExact) content != r.contentValue else !content.contains(r.contentValue, true)) return
    }

    matchCnt++
    LogManager.ok("══════ 第 $matchCnt 条 ══════")
    LogManager.i("来源: $appName | 标题: $title | 内容: $content")
    LogManager.i("正在推送...")

    viewModelScope.launch {
      Sender.sendAll(getApplication(), r, MsgData(pkg, title, content))
    }
  }
}
