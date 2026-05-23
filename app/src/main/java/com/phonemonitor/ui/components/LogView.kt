package com.phonemonitor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonemonitor.ui.theme.AccentGreen
import com.phonemonitor.ui.theme.AccentOrange
import com.phonemonitor.ui.theme.AccentRed
import com.phonemonitor.ui.theme.LogBg
import com.phonemonitor.ui.theme.LogBorder
import com.phonemonitor.ui.theme.TextSecondary
import com.phonemonitor.ui.theme.TextTertiary
import com.phonemonitor.ui.theme.White

@Composable
fun LogView(logs: List<LogEntry>, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(LogBg)
      .padding(8.dp)
  ) {
    if (logs.isEmpty()) {
      Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("暂无日志", color = TextTertiary, fontSize = 14.sp)
          Spacer(Modifier.height(4.dp))
          Text("点击启动开始监控", color = TextTertiary.copy(alpha = 0.6f), fontSize = 11.sp)
        }
      }
    } else {
      LazyColumn(Modifier.fillMaxSize()) { items(logs) { LogItem(it) } }
    }
  }
}

@Composable
private fun LogItem(e: LogEntry) {
  val dotColor = when (e.type) { LogType.INFO -> TextSecondary; LogType.SUCCESS -> AccentGreen; LogType.ERROR -> AccentRed; LogType.WARNING -> AccentOrange }
  val tag = when (e.type) { LogType.INFO -> "INF"; LogType.SUCCESS -> "OK"; LogType.ERROR -> "ERR"; LogType.WARNING -> "WRN" }
  Column(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(Modifier.size(6.dp).clip(CircleShape).background(dotColor))
      Spacer(Modifier.width(6.dp))
      Text(e.timestamp, color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
      Spacer(Modifier.width(6.dp))
      Text(tag, color = dotColor, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
    Spacer(Modifier.height(2.dp))
    Text(e.message, color = when (e.type) { LogType.ERROR -> AccentRed; LogType.WARNING -> AccentOrange; else -> androidx.compose.ui.graphics.Color(0xFF3C3C43) }, fontSize = 12.sp, fontFamily = FontFamily.Monospace, lineHeight = 16.sp)
  }
}
