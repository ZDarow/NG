package com.v2ray.ang.util

import com.v2ray.ang.AppConfig
import com.v2ray.ang.handler.MmkvManager
import timber.log.Timber
import java.util.Locale

/**
 * Legacy logging utility. Delegates to Timber internally.
 * New code should use Timber directly: Timber.tag("TAG").d("msg")
 */
object LogUtil {

    private const val DEFAULT_LEVEL = "warning"
    private const val CACHE_UNSET = Int.MIN_VALUE

    @Volatile
    private var cachedMinPriority: Int = CACHE_UNSET

    private fun parsePriority(level: String?): Int {
        return when ((level ?: DEFAULT_LEVEL).lowercase(Locale.US)) {
            "verbose" -> LogPriority.VERBOSE
            "debug" -> LogPriority.DEBUG
            "info" -> LogPriority.INFO
            "warn", "warning" -> LogPriority.WARN
            "error" -> LogPriority.ERROR
            "none", "off" -> LogPriority.NONE
            else -> LogPriority.WARN
        }
    }

    @Suppress("unused")
    fun refreshLogLevel() {
        cachedMinPriority = parsePriority(MmkvManager.decodeSettingsString(AppConfig.PREF_LOGLEVEL, DEFAULT_LEVEL))
    }

    private fun minPriority(): Int {
        val cached = cachedMinPriority
        if (cached != CACHE_UNSET) {
            return cached
        }

        return synchronized(this) {
            val current = cachedMinPriority
            if (current != CACHE_UNSET) {
                current
            } else {
                parsePriority(MmkvManager.decodeSettingsString(AppConfig.PREF_LOGLEVEL, DEFAULT_LEVEL)).also {
                    cachedMinPriority = it
                }
            }
        }
    }

    private fun isEnabled(priority: Int): Boolean {
        return priority >= minPriority()
    }

    private object LogPriority {
        const val VERBOSE = 0
        const val DEBUG = 1
        const val INFO = 2
        const val WARN = 3
        const val ERROR = 4
        const val NONE = 5
    }

    private fun logToTimber(priority: Int, tag: String, message: String, throwable: Throwable? = null) {
        if (!isEnabled(priority)) return
        val t = if (tag != AppConfig.TAG) Timber.tag(tag) else Timber

        when (priority) {
            LogPriority.DEBUG -> if (throwable != null) t.d(throwable, message) else t.d(message)
            LogPriority.INFO -> if (throwable != null) t.i(throwable, message) else t.i(message)
            LogPriority.WARN -> if (throwable != null) t.w(throwable, message) else t.w(message)
            LogPriority.ERROR -> if (throwable != null) t.e(throwable, message) else t.e(message)
            else -> if (throwable != null) t.v(throwable, message) else t.v(message)
        }
    }

    fun d(tag: String = AppConfig.TAG, message: String) = logToTimber(LogPriority.DEBUG, tag, message)
    fun i(tag: String = AppConfig.TAG, message: String) = logToTimber(LogPriority.INFO, tag, message)
    fun w(tag: String = AppConfig.TAG, message: String) = logToTimber(LogPriority.WARN, tag, message)
    fun e(tag: String = AppConfig.TAG, message: String) = logToTimber(LogPriority.ERROR, tag, message)

    fun d(tag: String = AppConfig.TAG, message: String, throwable: Throwable) = logToTimber(LogPriority.DEBUG, tag, message, throwable)
    fun i(tag: String = AppConfig.TAG, message: String, throwable: Throwable) = logToTimber(LogPriority.INFO, tag, message, throwable)
    fun w(tag: String = AppConfig.TAG, message: String, throwable: Throwable) = logToTimber(LogPriority.WARN, tag, message, throwable)
    fun e(tag: String = AppConfig.TAG, message: String, throwable: Throwable) = logToTimber(LogPriority.ERROR, tag, message, throwable)
}

