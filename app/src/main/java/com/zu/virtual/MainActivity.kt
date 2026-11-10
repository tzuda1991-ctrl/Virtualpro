package com.zu.virtual

import android.app.ActivityManager
import android.os.Build
import android.util.AtomicFile
import java.io.File
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import java.util.concurrent.Executors
import top.niunaijun.blackbox.BlackBoxCore

class MainActivity : Activity() {
    private val worker = Executors.newSingleThreadExecutor()
    private lateinit var root: LinearLayout
    private lateinit var library: LinearLayout
    private lateinit var status: TextView
    private var busy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = column()
        val pad = (20 * resources.displayMetrics.density).toInt()
        root.setPadding(pad, pad, pad, pad)
        root.setBackgroundColor(Color.rgb(18, 24, 39))
        root.addView(label("ZU Virtual", 28f))
        root.addView(button("Add from phone") { chooseApp() })
        root.addView(button("Import APK") {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/vnd.android.package-archive"
            }, 1)
        })
        root.addView(button("Play Store") { openGoogle(false) })
        root.addView(button("Play Games") { openGoogle(true) })
        root.addView(button("Play crash details") { showCrashDetails() })
        status = label("", 14f)
        root.addView(status)
        root.addView(label("My apps", 21f))
        library = column()
        root.addView(library)
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun column() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    private fun label(value: String, size: Float) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(Color.WHITE)
        setPadding(0, 12, 0, 12)
    }
    private fun button(value: String, action: () -> Unit) = Button(this).apply {
        text = value
        isAllCaps = false
        setOnClickListener { if (!busy) action() }
    }

    private fun <T> runTask(message: String, work: () -> T, done: (T) -> Unit) {
        if (busy) return
        busy = true
        status.text = message
        worker.execute {
            val result = runCatching(work)
            runOnUiThread {
                if (isDestroyed || isFinishing) return@runOnUiThread
                busy = false
                status.text = ""
                result.fold(done) { status.text = it.localizedMessage ?: "Virtual runtime failed." }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun nameOf(info: ApplicationInfo) =
        runCatching { info.loadLabel(packageManager).toString() }.getOrDefault(info.packageName)

    private fun refresh(message: String = "") {
        runTask("Loading virtual apps…", {
            BlackBoxCore.get().getInstalledApplications(0, ZuEngine.USER)
                .filter { BlackBoxCore.getBPackageManager().getLaunchIntentForPackage(it.packageName, ZuEngine.USER) != null }
                .map { it.packageName to nameOf(it) }.sortedBy { it.second.lowercase() }
        }) { apps ->
            library.removeAllViews()
            if (apps.isEmpty()) library.addView(label("Add an app from your phone or import an APK.", 15f))
            for ((pkg, name) in apps) library.addView(button(name) { launch(pkg) })
            status.text = message
        }
    }

    private fun launch(pkg: String) {
        runTask("Opening app…", { ZuEngine.launch(pkg) }) { ok ->
            if (!ok) status.text = "The virtual runtime could not launch $pkg."
        }
    }

    private fun chooseApp() {
        runTask("Finding apps and games…", {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            packageManager.queryIntentActivities(intent, 0).map { it.activityInfo.applicationInfo }
                .filter { it.packageName != packageName }.distinctBy { it.packageName }
                .map { it.packageName to nameOf(it) }.sortedBy { it.second.lowercase() }
        }) { apps ->
            if (apps.isEmpty()) status.text = "No launcher apps found on this phone."
            else AlertDialog.Builder(this).setTitle("Add an app or game")
                .setItems(apps.map { "${it.second}\n${it.first}" }.toTypedArray()) { _, index ->
                    val (pkg, name) = apps[index]
                    runTask("Adding $name…", {
                        if (!ZuEngine.isInstalled(pkg)) requireSuccess(ZuEngine.install(pkg))
                    }) { refresh("$name is in your virtual space.") }
                }.setNegativeButton("Cancel", null).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 1 || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        runTask("Installing APK…", {
            val result = BlackBoxCore.get().installPackageAsUser(uri, ZuEngine.USER)
            check(result.success) { result.msg ?: "APK installation failed." }
            result.packageName
        }) { refresh("Installed $it.") }
    }

    private fun requireSuccess(result: Pair<Boolean, String>) {
        check(result.first) { result.second.ifBlank { "Virtual installation failed." } }
    }

    private fun showCrashDetails() {
        val stack = runCatching {
            AtomicFile(File(filesDir, "play-crash.txt")).openRead().bufferedReader().use { it.readText() }
        }.getOrNull()
        val exits = if (Build.VERSION.SDK_INT >= 30) runCatching {
            (getSystemService(ACTIVITY_SERVICE) as ActivityManager)
                .getHistoricalProcessExitReasons(packageName, 0, 12)
                .filter { it.reason in listOf(android.app.ApplicationExitInfo.REASON_CRASH,
                        android.app.ApplicationExitInfo.REASON_CRASH_NATIVE, android.app.ApplicationExitInfo.REASON_ANR) }
                .take(3).joinToString("\n") {
                    "${java.util.Date(it.timestamp)}  ${it.processName}  reason=${it.reason} status=${it.status}"
                }
        }.getOrNull() else null
        val bindError = runCatching {
            AtomicFile(File(filesDir, "google-bind-error.txt")).openRead().bufferedReader().use { it.readText() }
        }.getOrNull()
        val report = listOfNotNull(bindError, stack, exits).filter { it.isNotBlank() }.joinToString("\n")
        AlertDialog.Builder(this).setTitle("Play crash details")
            .setMessage(report.ifEmpty { "No crash recorded yet. Open Play Store, then check here again." })
            .setPositiveButton("Close", null).apply {
                if (report.isNotEmpty()) setNeutralButton("Copy") { _, _ ->
                    (getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager)
                        .setPrimaryClip(android.content.ClipData.newPlainText("Play crash", report))
                }
            }.show()
    }

    private fun openGoogle(games: Boolean) {
        val target = if (games) "com.google.android.play.games" else "com.android.vending"
        runTask("Preparing Google Play…", {
            for (pkg in listOf("com.google.android.gsf", "com.google.android.gms", target)) {
                if (ZuEngine.isInstalled(pkg)) continue
                check(runCatching { packageManager.getApplicationInfo(pkg, 0) }.isSuccess) {
                    "$pkg is missing on this phone. Google Play setup needs this package."
                }
            }
            if (!ZuEngine.isGmsInstalled() || !ZuEngine.isInstalled("com.android.vending"))
                requireSuccess(ZuEngine.installGms())
            if (!ZuEngine.isInstalled(target)) requireSuccess(ZuEngine.install(target))
        }) { launch(target) }
    }

    override fun onDestroy() {
        worker.shutdown()
        super.onDestroy()
    }
}
