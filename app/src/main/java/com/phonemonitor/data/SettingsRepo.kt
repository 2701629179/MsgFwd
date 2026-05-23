package com.phonemonitor.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class SettingsRepo(context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("notimon_config", Context.MODE_PRIVATE)

  fun loadRule(): RuleConfig {
    val r = RuleConfig()
    r.selectedPkg = prefs.getString("selPkg", "") ?: ""
    r.selectedAppName = prefs.getString("selApp", "") ?: ""
    r.titleEnabled = prefs.getBoolean("titleEn", false)
    r.titleValue = prefs.getString("titleVal", "") ?: ""
    r.titleExact = prefs.getBoolean("titleExact", false)
    r.contentEnabled = prefs.getBoolean("contentEn", false)
    r.contentValue = prefs.getString("contentVal", "") ?: ""
    r.contentExact = prefs.getBoolean("contentExact", false)
    r.successCode = prefs.getInt("successCode", 200)

    val chJson = prefs.getString("channels", "[]") ?: "[]"
    try {
      val arr = JSONArray(chJson)
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        val c = ChannelConfig(SendType.valueOf(o.getString("type")))
        c.enabled = o.optBoolean("enabled", false)
        c.smtpHost = o.optString("smtpHost", "")
        c.smtpPort = o.optInt("smtpPort", 587)
        c.smtpUser = o.optString("smtpUser", "")
        c.smtpPass = o.optString("smtpPass", "")
        c.smtpFrom = o.optString("smtpFrom", "")
        c.smtpTo = o.optString("smtpTo", "")
        c.webhookUrl = o.optString("webhookUrl", "")
        c.webhookMethod = o.optString("webhookMethod", "POST")
        c.urlScheme = o.optString("urlScheme", "")
        c.includeContent = o.optBoolean("includeContent", true)
        c.successCode = o.optInt("successCode", 200)
        r.channels.add(c)
      }
    } catch (_: Exception) {}
    return r
  }

  fun saveRule(r: RuleConfig) {
    prefs.edit().apply {
      putString("selPkg", r.selectedPkg)
      putString("selApp", r.selectedAppName)
      putBoolean("titleEn", r.titleEnabled)
      putString("titleVal", r.titleValue)
      putBoolean("titleExact", r.titleExact)
      putBoolean("contentEn", r.contentEnabled)
      putString("contentVal", r.contentValue)
      putBoolean("contentExact", r.contentExact)
      putInt("successCode", r.successCode)

      val arr = JSONArray()
      for (c in r.channels) {
        val o = JSONObject()
        o.put("type", c.type.name)
        o.put("enabled", c.enabled)
        o.put("smtpHost", c.smtpHost)
        o.put("smtpPort", c.smtpPort)
        o.put("smtpUser", c.smtpUser)
        o.put("smtpPass", c.smtpPass)
        o.put("smtpFrom", c.smtpFrom)
        o.put("smtpTo", c.smtpTo)
        o.put("webhookUrl", c.webhookUrl)
        o.put("webhookMethod", c.webhookMethod)
        o.put("urlScheme", c.urlScheme)
        o.put("includeContent", c.includeContent)
        o.put("successCode", c.successCode)
        arr.put(o)
      }
      putString("channels", arr.toString())
      apply()
    }
  }

  fun getExportJson(): String {
    val r = loadRule()
    return r.toJson()
  }

  fun importFromJson(json: String) {
    val r = RuleConfig.fromJson(json)
    if (r.selectedPkg.isNotEmpty() || r.titleEnabled || r.contentEnabled) {
      saveRule(r)
    }
  }
}
