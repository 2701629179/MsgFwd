package com.phonemonitor.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.asImageBitmap

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.phonemonitor.MainViewModel
import com.phonemonitor.data.ChannelConfig
import com.phonemonitor.data.RuleConfig
import com.phonemonitor.data.SendType
import com.phonemonitor.log.LogManager
import com.phonemonitor.monitor.AppInfo
import com.phonemonitor.ui.components.LogView
import com.phonemonitor.ui.theme.AccentBlue
import com.phonemonitor.ui.theme.AccentGreen
import com.phonemonitor.ui.theme.AccentOrange
import com.phonemonitor.ui.theme.AccentRed
import com.phonemonitor.ui.theme.CardBg
import com.phonemonitor.ui.theme.CardBorder
import com.phonemonitor.ui.theme.TextPrimary
import com.phonemonitor.ui.theme.TextSecondary
import com.phonemonitor.ui.theme.TextTertiary
import com.phonemonitor.ui.theme.White

@Composable
fun MainScreen(vm: MainViewModel) {
  val running by vm.running.collectAsState()
  val logs by vm.logs.collectAsState()
  val rule by vm.rule.collectAsState()
  val showApps by vm.showApps.collectAsState()
  val saveTs by vm.saveTs.collectAsState()
  var showSaveFlash by remember { mutableStateOf(false) }
  val toggleTs by vm.toggleTs.collectAsState()
  val toggleMsg by vm.toggleMsg.collectAsState()
  var showToggleFlash by remember { mutableStateOf(false) }
  val ctx = LocalContext.current

  LaunchedEffect(saveTs) {
    if (saveTs > 0) { showSaveFlash = true; delay(1200); showSaveFlash = false }
  }
  LaunchedEffect(toggleTs) {
    if (toggleTs > 0) { showToggleFlash = true; delay(1200); showToggleFlash = false }
  }

  Box(Modifier.fillMaxSize().background(CardBg)) {
    LazyColumn(
      Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 96.dp, bottom = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Text("通知监控", color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("实时监控 · 多通道推送 · 智能过滤", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 1.dp))
      }

      item {
        Button(
          onClick = { vm.toggle() },
          modifier = Modifier.fillMaxWidth().height(48.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = if (running) AccentBlue.copy(alpha = 0.12f) else AccentBlue)
        ) {
          Icon(if (running) Icons.Default.Stop else Icons.Default.PlayArrow, null, tint = if (running) AccentBlue else White, modifier = Modifier.size(20.dp))
          Spacer(Modifier.width(6.dp))
          Text(if (running) "停止监控" else "启动监控", color = if (running) AccentBlue else White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
      }

      item {
        SectionCard("监听应用", Icons.Default.Smartphone, AccentBlue) {
          Row(Modifier.fillMaxWidth().clickable { vm.showAppDialog() }.background(Color(0xFFF8F8F8), RoundedCornerShape(10.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
              Text(if (rule.selectedPkg.isNotEmpty()) "${rule.selectedAppName} (${rule.selectedPkg})" else "所有应用", color = if (rule.selectedPkg.isNotEmpty()) TextPrimary else TextSecondary, fontSize = 13.sp)
              Text(if (rule.selectedPkg.isNotEmpty()) "点击切换" else "不选则监听所有通知", color = TextTertiary, fontSize = 10.sp)
            }
            Icon(Icons.Default.KeyboardArrowDown, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
          }
        }
      }

      item {
        SectionCard("过滤条件", Icons.Default.FilterList, AccentOrange) {
          FilterGroup("标题过滤", rule.titleEnabled, rule.titleValue, rule.titleExact,
            { vm.updateRule(rule.copy(titleEnabled = it)) },
            { vm.updateRule(rule.copy(titleValue = it)) },
            { vm.updateRule(rule.copy(titleExact = it)) })
          Spacer(Modifier.height(8.dp))
          FilterGroup("内容过滤", rule.contentEnabled, rule.contentValue, rule.contentExact,
            { vm.updateRule(rule.copy(contentEnabled = it)) },
            { vm.updateRule(rule.copy(contentValue = it)) },
            { vm.updateRule(rule.copy(contentExact = it)) })
        }
      }

      item {
        SectionCard("发送通道", Icons.Default.Extension, AccentGreen) {
          rule.channels.forEachIndexed { i, ch -> ChannelCard(ch, i, vm) }
          Spacer(Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(SendType.EMAIL to "邮箱", SendType.WEBHOOK to "Webhook", SendType.URL to "URL").forEach { (t, l) ->
              androidx.compose.material3.Surface(
                onClick = { vm.addChannel(t) },
                modifier = Modifier.weight(1f).height(38.dp),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentBlue),
                color = Color.Transparent
              ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                  Text("+ $l", fontSize = 12.sp, color = AccentBlue)
                }
              }
            }
          }
        }
      }

      item {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(onClick = {
            val json = vm.exportRules()
            val clip = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clip.setPrimaryClip(android.content.ClipData.newPlainText("rule", json))
            Toast.makeText(ctx, "规则已复制到剪贴板", Toast.LENGTH_SHORT).show()
          }, modifier = Modifier.weight(1f).height(38.dp), shape = RoundedCornerShape(8.dp)) {
            Icon(Icons.Default.FileUpload, null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(2.dp)); Text("导出", fontSize = 12.sp)
          }
          OutlinedButton(onClick = {
            val clip = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val data = clip.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
            if (data.isNotEmpty()) vm.importRules(data) else Toast.makeText(ctx, "剪贴板为空", Toast.LENGTH_SHORT).show()
          }, modifier = Modifier.weight(1f).height(38.dp), shape = RoundedCornerShape(8.dp)) {
            Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(2.dp)); Text("导入", fontSize = 12.sp)
          }
          OutlinedButton(onClick = { vm.requestBatteryOpt() }, modifier = Modifier.weight(1f).height(38.dp), shape = RoundedCornerShape(8.dp)) {
            Icon(Icons.Default.Settings, null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(2.dp)); Text("电池", fontSize = 12.sp)
          }
        }
      }

      item {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("运行日志", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(6.dp))
            Text("${logs.size} 条", color = TextSecondary, fontSize = 11.sp)
          }
          Box(Modifier.clickable { LogManager.clear() }.padding(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.DeleteSweep, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
              Spacer(Modifier.width(4.dp)); Text("清空", color = TextSecondary, fontSize = 11.sp)
            }
          }
        }
      }
      item {
        Card(Modifier.fillMaxWidth().height(300.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardBg), elevation = CardDefaults.cardElevation(0.dp)) {
          LogView(logs, Modifier.fillMaxSize())
        }
      }
      item { Spacer(Modifier.height(8.dp)) }
    }

    if (showApps) {
      val filtered by vm.filteredApps.collectAsState()
      val appSearch by vm.appSearch.collectAsState()
      val showSys by vm.showSys.collectAsState()

      AlertDialog(
        onDismissRequest = { vm.hideAppDialog() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.94f).height(520.dp),
        title = { Text("选择应用", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
        text = {
          Column {
            OutlinedTextField(
              value = appSearch, onValueChange = { vm.setAppSearch(it) },
              modifier = Modifier.fillMaxWidth(), placeholder = { Text("搜索应用...", fontSize = 13.sp, color = TextTertiary) },
              singleLine = true,
              leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
              colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = AccentBlue, focusedBorderColor = AccentBlue, unfocusedBorderColor = CardBorder),
              shape = RoundedCornerShape(10.dp),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = TextPrimary)
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Switch(checked = showSys, onCheckedChange = { vm.setShowSys(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = White, checkedTrackColor = AccentBlue))
              Spacer(Modifier.width(6.dp))
              Text("显示系统应用", fontSize = 12.sp, color = TextSecondary)
              Spacer(Modifier.weight(1f))
              Text("共 ${filtered.size} 个", fontSize = 11.sp, color = TextTertiary)
            }
            Spacer(Modifier.height(6.dp))
            LazyColumn(Modifier.fillMaxWidth()) {
              items(filtered) { app ->
                Row(
                  Modifier.fillMaxWidth().clickable { vm.selectApp(app) }.padding(vertical = 6.dp, horizontal = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  if (app.icon != null) {
                    val bmp = remember(app) {
                      val d = app.icon ?: return@remember null
                      val b = android.graphics.Bitmap.createBitmap(d.intrinsicWidth.coerceAtLeast(1), d.intrinsicHeight.coerceAtLeast(1), android.graphics.Bitmap.Config.ARGB_8888)
                      val c = android.graphics.Canvas(b); d.setBounds(0, 0, c.width, c.height); d.draw(c); b
                    }
                    if (bmp != null) {
                      androidx.compose.foundation.Image(bitmap = bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.size(28.dp).clip(CircleShape))
                    } else { Box(Modifier.size(28.dp).clip(CircleShape).background(AccentBlue.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Text(app.appName.take(1), color = AccentBlue, fontSize = 12.sp) } }
                  } else {
                    Box(Modifier.size(28.dp).clip(CircleShape).background(AccentBlue.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                      Text(app.appName.take(1), color = AccentBlue, fontSize = 12.sp)
                    }
                  }
                  Spacer(Modifier.width(10.dp))
                  Column(Modifier.weight(1f)) {
                    Text(app.appName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(app.packageName, color = TextSecondary, fontSize = 10.sp)
                  }
                  if (app.isSystem) {
                    Box(Modifier.clip(RoundedCornerShape(4.dp)).background(AccentOrange.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                      Text("系统", color = AccentOrange, fontSize = 9.sp)
                    }
                  }
                }
              }
            }
          }
        },
        confirmButton = { TextButton(onClick = { vm.hideAppDialog() }) { Text("取消") } }
      )
    }

    AnimatedVisibility(
      visible = showSaveFlash,
      enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(300)),
      exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(500)),
      modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
    ) {
      Box(Modifier.background(Color(0xE6000000).copy(alpha = 0.75f), RoundedCornerShape(20.dp)).padding(horizontal = 20.dp, vertical = 10.dp)) {
        Text("保存成功", color = White, fontSize = 14.sp)
      }
    }

    AnimatedVisibility(
      visible = showToggleFlash,
      enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(300)),
      exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(500)),
      modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 72.dp)
    ) {
      Box(Modifier.background(Color(0xE6000000).copy(alpha = 0.75f), RoundedCornerShape(20.dp)).padding(horizontal = 20.dp, vertical = 10.dp)) {
        Text(toggleMsg, color = White, fontSize = 14.sp)
      }
    }
  }
}

@Composable
private fun SectionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, content: @Composable () -> Unit) {
  Card(
    Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = CardBg),
    elevation = CardDefaults.cardElevation(0.dp),
    border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
  ) {
    Column(Modifier.padding(14.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
      }
      Spacer(Modifier.height(8.dp))
      content()
    }
  }
}

@Composable
private fun FilterGroup(label: String, enabled: Boolean, value: String, exact: Boolean, onEn: (Boolean) -> Unit, onVal: (String) -> Unit, onExact: (Boolean) -> Unit) {
  var showDrop by remember { mutableStateOf(false) }
  Column {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      Switch(checked = enabled, onCheckedChange = onEn,
        colors = SwitchDefaults.colors(checkedThumbColor = White, checkedTrackColor = AccentBlue, uncheckedThumbColor = White, uncheckedTrackColor = Color(0xFFE5E5EA)))
      Spacer(Modifier.width(6.dp))
      Text(label, fontSize = 13.sp, color = if (enabled) AccentBlue else TextSecondary)
      Spacer(Modifier.weight(1f))
      if (enabled) {
        Box {
          TextButton(onClick = { showDrop = true }, modifier = Modifier.height(28.dp)) {
            Text(if (exact) "完全相等" else "包含内容", color = AccentBlue, fontSize = 11.sp)
            Icon(Icons.Default.KeyboardArrowDown, null, tint = AccentBlue, modifier = Modifier.size(14.dp))
          }
          DropdownMenu(expanded = showDrop, onDismissRequest = { showDrop = false }) {
            DropdownMenuItem(text = { Text("完全相等", color = TextPrimary, fontSize = 13.sp) }, onClick = { onExact(true); showDrop = false })
            DropdownMenuItem(text = { Text("包含内容", color = TextPrimary, fontSize = 13.sp) }, onClick = { onExact(false); showDrop = false })
          }
        }
      }
    }
    AnimatedVisibility(visible = enabled, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
      OutlinedTextField(
        value = value, onValueChange = onVal,
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        placeholder = { Text("输入${label}关键字...", color = TextTertiary, fontSize = 13.sp) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = AccentBlue, focusedBorderColor = AccentBlue, unfocusedBorderColor = CardBorder),
        shape = RoundedCornerShape(8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = TextPrimary)
      )
    }
  }
}

@Composable
private fun ChannelCard(cfg: ChannelConfig, idx: Int, vm: MainViewModel) {
  var expanded by remember { mutableStateOf(false) }
  val label = when (cfg.type) { SendType.EMAIL -> "邮箱 (SMTP)"; SendType.WEBHOOK -> "Webhook"; SendType.URL -> "URL 跳转" }
  val color = when (cfg.type) { SendType.EMAIL -> AccentBlue; SendType.WEBHOOK -> AccentOrange; SendType.URL -> AccentGreen }

  Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)), elevation = CardDefaults.cardElevation(0.dp)) {
    Column(Modifier.padding(10.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(checked = cfg.enabled, onCheckedChange = { vm.updateChannel(idx, cfg.copy(enabled = it)) },
          colors = SwitchDefaults.colors(checkedThumbColor = White, checkedTrackColor = color))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (cfg.enabled) TextPrimary else TextSecondary)
        Spacer(Modifier.weight(1f))
        IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(24.dp)) {
          Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = { vm.removeChannel(idx) }, modifier = Modifier.size(24.dp)) {
          Icon(Icons.Default.Close, null, tint = AccentRed, modifier = Modifier.size(16.dp))
        }
      }

      AnimatedVisibility(visible = expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
        Column(Modifier.padding(top = 8.dp)) {
          when (cfg.type) {
            SendType.EMAIL -> EmailFields(cfg, idx, vm)
            SendType.WEBHOOK -> WebhookFields(cfg, idx, vm)
            SendType.URL -> UrlFields(cfg, idx, vm)
          }
          Spacer(Modifier.height(4.dp))
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text("成功状态码:", fontSize = 11.sp, color = TextSecondary)
            Spacer(Modifier.width(6.dp))
            OutlinedTextField(
              value = cfg.successCode.toString(), onValueChange = { it.toIntOrNull()?.let { v -> vm.updateChannel(idx, cfg.copy(successCode = v)) } },
              modifier = Modifier.width(64.dp),
              singleLine = true,
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = TextPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue, unfocusedBorderColor = CardBorder),
              shape = RoundedCornerShape(6.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(Modifier.width(4.dp))
            Text("默认200", fontSize = 10.sp, color = TextTertiary)
          }
        }
      }
    }
  }
}

@Composable
private fun EmailFields(cfg: ChannelConfig, idx: Int, vm: MainViewModel) {
  Field("SMTP 服务器", cfg.smtpHost) { vm.updateChannel(idx, cfg.copy(smtpHost = it)) }
  Spacer(Modifier.height(4.dp))
  Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
    Field("端口", cfg.smtpPort.toString(), Modifier.weight(1f)) { it.toIntOrNull()?.let { v -> vm.updateChannel(idx, cfg.copy(smtpPort = v)) } }
    Field("用户名", cfg.smtpUser, Modifier.weight(1f)) { vm.updateChannel(idx, cfg.copy(smtpUser = it)) }
  }
  Spacer(Modifier.height(4.dp))
  Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
    Field("密码", cfg.smtpPass, Modifier.weight(1f)) { vm.updateChannel(idx, cfg.copy(smtpPass = it)) }
    Field("发件人", cfg.smtpFrom, Modifier.weight(1f)) { vm.updateChannel(idx, cfg.copy(smtpFrom = it)) }
  }
  Spacer(Modifier.height(4.dp))
  Field("收件人", cfg.smtpTo) { vm.updateChannel(idx, cfg.copy(smtpTo = it)) }
  Text("提示: 可在收件人中使用 [msg] 自动替换内容", color = TextTertiary, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
}

@Composable
private fun WebhookFields(cfg: ChannelConfig, idx: Int, vm: MainViewModel) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text("请求方式:", fontSize = 11.sp, color = TextSecondary)
    Spacer(Modifier.width(6.dp))
    var showDrop by remember { mutableStateOf(false) }
    Box {
      TextButton(onClick = { showDrop = true }, modifier = Modifier.height(30.dp)) {
        Text(cfg.webhookMethod, color = AccentBlue, fontSize = 12.sp)
        Icon(Icons.Default.KeyboardArrowDown, null, tint = AccentBlue, modifier = Modifier.size(14.dp))
      }
      DropdownMenu(expanded = showDrop, onDismissRequest = { showDrop = false }) {
        listOf("POST", "GET", "PUT", "PATCH").forEach { m ->
          DropdownMenuItem(text = { Text(m, color = TextPrimary, fontSize = 12.sp) }, onClick = { vm.updateChannel(idx, cfg.copy(webhookMethod = m)); showDrop = false })
        }
      }
    }
  }
  Spacer(Modifier.height(4.dp))
  Field("Webhook URL", cfg.webhookUrl) { vm.updateChannel(idx, cfg.copy(webhookUrl = it)) }
  Text("示例: https://a.b.com/mas?token=[msg]", color = TextTertiary, fontSize = 10.sp)
  Text("提示: 使用 [msg] 自动替换为内容", color = TextTertiary, fontSize = 10.sp)
}

@Composable
private fun UrlFields(cfg: ChannelConfig, idx: Int, vm: MainViewModel) {
  Field("URL 协议链接", cfg.urlScheme) { vm.updateChannel(idx, cfg.copy(urlScheme = it)) }
  Text("示例: myapp://api/add?type=0&msg=[msg]", color = TextTertiary, fontSize = 10.sp)
  Text("提示: 使用 [msg] 自动替换为内容", color = TextTertiary, fontSize = 10.sp)
  Text("      发送后自动跳转到该链接", color = TextTertiary, fontSize = 10.sp)
}

@Composable
private fun Field(label: String, value: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
  OutlinedTextField(
    value = value, onValueChange = onChange,
    modifier = modifier.fillMaxWidth(),
    label = { Text(label, fontSize = 11.sp, color = TextTertiary) },
    singleLine = true,
    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = AccentBlue, focusedBorderColor = AccentBlue, unfocusedBorderColor = CardBorder, focusedLabelColor = AccentBlue, unfocusedLabelColor = TextTertiary),
      shape = RoundedCornerShape(6.dp),
      textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = TextPrimary)
    )
}
