package com.phonemonitor.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.phonemonitor.ui.theme.AccentBlue
import com.phonemonitor.ui.theme.AccentRed
import com.phonemonitor.ui.theme.CardBg
import com.phonemonitor.ui.theme.CardBorder
import com.phonemonitor.ui.theme.TextPrimary
import com.phonemonitor.ui.theme.TextSecondary
import com.phonemonitor.ui.theme.TextTertiary
import java.io.File

private val DrawerWidth = 280.dp

@Composable
private fun AppIcon(modifier: Modifier = Modifier) {
  Image(
    painter = androidx.compose.ui.res.painterResource(com.phonemonitor.R.drawable.ic_app_icon),
    contentDescription = null,
    modifier = modifier,
    contentScale = ContentScale.Fit
  )
}

@Composable
fun DrawerScaffold(
  onMenuToggle: () -> Unit,
  showAbout: Boolean,
  onShowAboutChange: (Boolean) -> Unit,
  content: @Composable () -> Unit
) {
  var isOpen by remember { mutableStateOf(false) }

  if (showAbout) AboutDialog(onDismiss = { onShowAboutChange(false) })

  Box(Modifier.fillMaxSize()) {
    Box(Modifier.fillMaxSize()) {
      content()
    }

    AnimatedVisibility(
      visible = isOpen,
      enter = fadeIn(tween(300)),
      exit = fadeOut(tween(300))
    ) {
      Box(
        Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.30f))
          .clickable { isOpen = false }
      )
    }

    AnimatedVisibility(
      visible = isOpen,
      enter = slideInHorizontally(tween(300)) { -it },
      exit = slideOutHorizontally(tween(300)) { -it }
    ) {
      Box(
        Modifier
          .width(DrawerWidth)
          .fillMaxHeight()
          .background(CardBg)
      ) {
        DrawerPanel(onAbout = { onShowAboutChange(true) })
      }
    }

    if (!isOpen) {
      IconButton(
        onClick = { isOpen = true },
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(top = 48.dp, start = 8.dp)
          .size(40.dp)
      ) {
        Icon(Icons.Default.Menu, "菜单", tint = AccentBlue, modifier = Modifier.size(24.dp))
      }
    }
  }
}

@Composable
private fun DrawerPanel(onAbout: () -> Unit) {
  val ctx = LocalContext.current
  var cacheSize by remember { mutableLongStateOf(0L) }

  fun refresh() { cacheSize = calculateCacheSize(ctx.cacheDir) }

  LaunchedEffect(Unit) { refresh() }

  Column(
    Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp)
  ) {
    Spacer(Modifier.height(60.dp))

    Box(
      Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(AccentBlue),
      contentAlignment = Alignment.Center
    ) {
      AppIcon(Modifier.fillMaxSize())
    }
    Spacer(Modifier.height(12.dp))
    Text("消息转发器", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Text("免费开源 · 禁止倒卖", color = TextSecondary, fontSize = 12.sp)
    Spacer(Modifier.height(24.dp))

    Divider(color = CardBorder, thickness = 0.5.dp)
    Spacer(Modifier.height(12.dp))

    DrawerMenuItem(Icons.Default.Storage, "缓存大小: ${formatSize(cacheSize)}") {
      try {
        ctx.cacheDir.deleteRecursively()
        File(ctx.cacheDir.parentFile, "code_cache").deleteRecursively()
        refresh()
        android.widget.Toast.makeText(ctx, "缓存已清理", android.widget.Toast.LENGTH_SHORT).show()
      } catch (_: Exception) { }
    }
    Spacer(Modifier.height(4.dp))

    DrawerMenuItem(Icons.Default.Person, "联系作者微信") {
      val clip = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
      clip.setPrimaryClip(android.content.ClipData.newPlainText("wx", "liushuheng06"))
      android.widget.Toast.makeText(ctx, "已复制微信号 liushuheng06", android.widget.Toast.LENGTH_SHORT).show()
    }
    Spacer(Modifier.height(4.dp))

    DrawerMenuItem(Icons.Default.MailOutline, "联系作者邮箱") {
      val clip = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
      clip.setPrimaryClip(android.content.ClipData.newPlainText("email", "2701629179@qq.com"))
      android.widget.Toast.makeText(ctx, "已复制作者邮箱 2701629179@qq.com", android.widget.Toast.LENGTH_SHORT).show()
    }
    Spacer(Modifier.height(4.dp))

    Divider(color = CardBorder, thickness = 0.5.dp)
    Spacer(Modifier.height(12.dp))

    DrawerMenuItem(Icons.Default.Info, "关于作者", onClick = onAbout)
    Spacer(Modifier.height(4.dp))

    Spacer(Modifier.weight(1f))
    Text("© 2026 消息转发器", color = TextTertiary, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp))
  }
}

@Composable
private fun DrawerMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(Color(0xFFF0F0F5), RoundedCornerShape(10.dp))
      .clickable(onClick = onClick)
      .padding(vertical = 10.dp, horizontal = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      Modifier.size(32.dp).clip(CircleShape).background(AccentBlue.copy(alpha = 0.1f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(icon, null, tint = AccentBlue, modifier = Modifier.size(18.dp))
    }
    Spacer(Modifier.width(12.dp))
    Text(
      label,
      color = TextPrimary,
      fontSize = 14.sp,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
  val ctx = LocalContext.current
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      Modifier
        .fillMaxWidth(0.85f)
        .background(CardBg, RoundedCornerShape(20.dp))
        .padding(28.dp)
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
          Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(AccentBlue),
          contentAlignment = Alignment.Center
        ) {
          AppIcon(Modifier.fillMaxSize())
        }
        Spacer(Modifier.height(12.dp))
        Text("消息转发器", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("开发不易 请勿倒卖", color = AccentRed, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(16.dp))
        Divider(color = CardBorder, thickness = 0.5.dp)
        Spacer(Modifier.height(16.dp))
        Text("作者: 小恒", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text("邮箱: 2701629179@qq.com", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.clickable {
          val clip = ctx.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
          clip.setPrimaryClip(android.content.ClipData.newPlainText("email", "2701629179@qq.com"))
          android.widget.Toast.makeText(ctx, "已复制邮箱地址", android.widget.Toast.LENGTH_SHORT).show()
        })
        Spacer(Modifier.height(24.dp))
        TextButton(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth().height(40.dp),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("知道了", color = AccentBlue, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}

private fun calculateCacheDirSize(dir: File): Long {
  if (!dir.exists()) return 0L
  return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
}

private fun calculateCacheSize(cacheDir: File): Long {
  var total = calculateCacheDirSize(cacheDir)
  val codeCache = File(cacheDir.parentFile, "code_cache")
  total += calculateCacheDirSize(codeCache)
  cacheDir.parentFile?.listFiles()?.forEach {
    if (it.isDirectory && it.name.startsWith("app_")) {
      total += calculateCacheDirSize(it)
    }
  }
  return total
}

private fun formatSize(bytes: Long): String {
  if (bytes < 1024) return "$bytes B"
  val kb = bytes / 1024.0
  if (kb < 1024) return "%.1f KB".format(kb)
  val mb = kb / 1024.0
  if (mb < 1024) return "%.1f MB".format(mb)
  val gb = mb / 1024.0
  return "%.1f GB".format(gb)
}
