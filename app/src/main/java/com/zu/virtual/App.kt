package com.zu.virtual

import android.app.Application
import android.content.Context
import android.util.AtomicFile
import java.io.File
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackbox.app.configuration.ClientConfiguration

class App : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        val report = AtomicFile(File(base.filesDir, "play-crash.txt"))
        BlackBoxCore.get().setExceptionHandler { _, error ->
            runCatching {
                val guest = BlackBoxCore.getAppPackageName() ?: return@runCatching
                if (guest !in setOf("com.android.vending", "com.google.android.gms", "com.google.android.gsf"))
                    return@runCatching
                val trace = buildString {
                    appendLine("$guest • ZU Virtual 1.4 • ${java.util.Date()}")
                    for (cause in generateSequence(error) { it.cause }.take(6)) {
                        appendLine(cause.javaClass.name)
                        if (cause is ClassCastException) appendLine(cause.message?.take(350))
                        cause.stackTrace.take(6).forEach { appendLine("  at $it") }
                    }
                }
                val out = report.startWrite()
                try {
                    out.write(trace.toByteArray())
                    report.finishWrite(out)
                } catch (e: Exception) {
                    report.failWrite(out)
                }
            }
        }
        BlackBoxCore.get().doAttachBaseContext(base, object : ClientConfiguration() {
            override fun getHostPackageName(): String = base.packageName
        })
    }

    override fun onCreate() {
        super.onCreate()
        BlackBoxCore.get().doCreate()
    }
}
