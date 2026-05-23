package com.phonemonitor.data

data class MsgData(
  val pkg: String, val title: String, val content: String
) {
  fun formatted() = buildString {
    appendLine("包名：$pkg")
    appendLine("通知标题：$title")
    appendLine("通知内容：$content")
  }

  fun msgParam() = "包名：$pkg\n通知标题：$title\n通知内容：$content"
}

enum class SendType { EMAIL, WEBHOOK, URL }

data class ChannelConfig(
  val type: SendType,
  var enabled: Boolean = false,
  var smtpHost: String = "",
  var smtpPort: Int = 465,
  var smtpUser: String = "",
  var smtpPass: String = "",
  var smtpFrom: String = "",
  var smtpTo: String = "",
  var webhookUrl: String = "",
  var webhookMethod: String = "POST",
  var urlScheme: String = "",
  var includeContent: Boolean = true,
  var successCode: Int = 200
) {
  fun buildBody(msg: MsgData): String {
    val text = if (includeContent) msg.msgParam() else msg.title
    val encoded = java.net.URLEncoder.encode(text, "UTF-8")
    return when (type) {
      SendType.EMAIL -> smtpTo.replace("[msg]", text)
      SendType.WEBHOOK -> webhookUrl.replace("[msg]", encoded)
      SendType.URL -> urlScheme.replace("[msg]", encoded)
    }
  }

  fun buildSubject(msg: MsgData): String {
    return "通知监控 - ${msg.pkg}"
  }
}

typealias EmailConfig = ChannelConfig
typealias WebhookConfig = ChannelConfig
typealias URLConfig = ChannelConfig

data class RuleConfig(
  var selectedPkg: String = "",
  var selectedAppName: String = "",
  var titleEnabled: Boolean = false,
  var titleValue: String = "",
  var titleExact: Boolean = false,
  var contentEnabled: Boolean = false,
  var contentValue: String = "",
  var contentExact: Boolean = false,
  var successCode: Int = 200,
  var channels: MutableList<ChannelConfig> = mutableListOf()
) {
  fun toJson(): String = buildString {
    appendLine("{")
    appendLine("  \"selectedPkg\": \"${esc(selectedPkg)}\",")
    appendLine("  \"selectedAppName\": \"${esc(selectedAppName)}\",")
    appendLine("  \"titleEnabled\": $titleEnabled,")
    appendLine("  \"titleValue\": \"${esc(titleValue)}\",")
    appendLine("  \"titleExact\": $titleExact,")
    appendLine("  \"contentEnabled\": $contentEnabled,")
    appendLine("  \"contentValue\": \"${esc(contentValue)}\",")
    appendLine("  \"contentExact\": $contentExact,")
    appendLine("  \"channels\": [")
    channels.forEachIndexed { i, c ->
      appendLine("    {")
      appendLine("      \"type\": \"${c.type.name}\",")
      appendLine("      \"enabled\": ${c.enabled},")
      appendLine("      \"smtpHost\": \"${esc(c.smtpHost)}\",")
      appendLine("      \"smtpPort\": ${c.smtpPort},")
      appendLine("      \"smtpUser\": \"${esc(c.smtpUser)}\",")
      appendLine("      \"smtpPass\": \"${esc(c.smtpPass)}\",")
      appendLine("      \"smtpFrom\": \"${esc(c.smtpFrom)}\",")
      appendLine("      \"smtpTo\": \"${esc(c.smtpTo)}\",")
      appendLine("      \"webhookUrl\": \"${esc(c.webhookUrl)}\",")
      appendLine("      \"webhookMethod\": \"${c.webhookMethod}\",")
      appendLine("      \"urlScheme\": \"${esc(c.urlScheme)}\",")
      appendLine("      \"includeContent\": ${c.includeContent},")
      appendLine("      \"successCode\": ${c.successCode}")
      appendLine("    }${if (i < channels.size - 1) "," else ""}")
    }
    appendLine("  ]")
    appendLine("}")
  }

  companion object {
    fun fromJson(json: String): RuleConfig {
      val r = RuleConfig()
      try {
        r.selectedPkg = regex("\"selectedPkg\"\\s*:\\s*\"(.*?)\"").find(json)?.groupValues?.getOrNull(1) ?: ""
        r.selectedAppName = regex("\"selectedAppName\"\\s*:\\s*\"(.*?)\"").find(json)?.groupValues?.getOrNull(1) ?: ""
        r.titleEnabled = regex("\"titleEnabled\"\\s*:\\s*(true|false)").find(json)?.groupValues?.getOrNull(1) == "true"
        r.titleValue = regex("\"titleValue\"\\s*:\\s*\"(.*?)\"").find(json)?.groupValues?.getOrNull(1) ?: ""
        r.titleExact = regex("\"titleExact\"\\s*:\\s*(true|false)").find(json)?.groupValues?.getOrNull(1) == "true"
        r.contentEnabled = regex("\"contentEnabled\"\\s*:\\s*(true|false)").find(json)?.groupValues?.getOrNull(1) == "true"
        r.contentValue = regex("\"contentValue\"\\s*:\\s*\"(.*?)\"").find(json)?.groupValues?.getOrNull(1) ?: ""
        r.contentExact = regex("\"contentExact\"\\s*:\\s*(true|false)").find(json)?.groupValues?.getOrNull(1) == "true"
      } catch (_: Exception) {}
      return r
    }

    private fun regex(s: String) = Regex(s)
  }
}

private fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\t", "\\t")
