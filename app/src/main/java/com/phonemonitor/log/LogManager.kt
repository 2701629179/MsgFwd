package com.phonemonitor.log

import com.phonemonitor.ui.components.LogEntry
import com.phonemonitor.ui.components.LogType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LogManager {
  private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
  val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()
  private val fmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

  fun i(m: String) = add(m, LogType.INFO)
  fun ok(m: String) = add(m, LogType.SUCCESS)
  fun e(m: String) = add(m, LogType.ERROR)
  fun w(m: String) = add(m, LogType.WARNING)

  private fun add(m: String, t: LogType) {
    _logs.value = _logs.value + LogEntry(fmt.format(Date()), m, t)
  }

  fun clear() { _logs.value = emptyList() }
}
