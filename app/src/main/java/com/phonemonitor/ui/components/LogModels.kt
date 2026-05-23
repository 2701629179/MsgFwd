package com.phonemonitor.ui.components

data class LogEntry(val timestamp: String, val message: String, val type: LogType)
enum class LogType { INFO, SUCCESS, ERROR, WARNING }
