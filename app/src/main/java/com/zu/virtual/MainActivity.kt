package com.zu.virtual

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import java.io.File

class MainActivity : Activity() {

    private val targetPackage = "com.Chillgaming.oneState"

    private val bgTop = Color.rgb(9, 13, 26)
    private val bgBottom = Color.rgb(20, 27, 46)
    private val card = Color.rgb(24, 32, 50)
    private val stroke = Color.rgb(44, 56, 82)
    private val dim = Color.rgb(156, 170, 192)
    private val blue = Color.rgb(65, 135, 255)
    private val violet = Color.rgb(106, 92, 255)
    private val green = Color.rgb(61, 220, 132)
    private val red = Color.rgb(255, 93, 93)
    private val amber = Color.rgb(255, 200, 87)

    private lateinit var icon: ImageView
    private lateinit var appName: TextView
    private lateinit var pkgLabel: TextView
    private lateinit var version: TextView
    private lateinit var statusText: TextView
    private lateinit var statusDot: View
    private lateinit var importButton: Button
    private lateinit var startButton: Button

    private val appDirectory: File
        get() = File(filesDir, "virtual_apps/$targetPackage")

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radius: Int, strokeColor: Int? = null) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (strokeColor != null) setStroke(dp(1), strokeColor)
        }

    private fun label(value: String, size: Float, color: Int, bold: Boolean = false) =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
        }

    private fun chip(iconRes: Int, text: String) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(12), dp(8), dp(15), dp(8))
        background = rounded(card, 24, stroke)
        addView(ImageView(this@MainActivity).apply { setImageResource(iconRes) },
            LinearLayout.LayoutParams(dp(18), dp(18)))
        addView(label(text, 12f, Color.WHITE, true).apply { setPadding(dp(8), 0, 0, 0) })
    }

    private fun actionButton(text: String, c1: Int, c2: Int): Button {
        val on = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(c1, c2))
            .apply { cornerRadius = dp(16).toFloat() }
        val off = rounded(Color.rgb(32, 40, 58), 16)
        val states = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_enabled), on)
            addState(intArrayOf(), off)
        }
        return Button(this).apply {
            this.text = text
            textSize = 14f
            letterSpacing = 0.08f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_enabled), intArrayOf()),
                intArrayOf(Color.WHITE, Color.rgb(107, 120, 144))))
            background = RippleDrawable(ColorStateList.valueOf(0x33FFFFFF), states, null)
            stateListAnimator = null
            elevation = 0f
        }
    }

    private fun setStatus(message: String, color: Int) {
        statusText.text = message
        (statusDot.background as GradientDrawable).setColor(color)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bgTop
        window.navigationBarColor = bgBottom
        buildUI()
        importZooba(false)
    }

    private fun buildUI() {
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(bgTop, bgBottom))
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(40), dp(22), dp(28))
        }

        // Header
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val badge = TextView(this).apply {
            text = "ZU"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(blue, violet)).apply { cornerRadius = dp(14).toFloat() }
        }
        header.addView(badge, LinearLayout.LayoutParams(dp(46), dp(46)))
        badge.setOnLongClickListener { copyLog(); true }
        val titles = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), 0, 0, 0)
        }
        titles.addView(label("ZU Virtual", 26f, Color.WHITE, true))
        titles.addView(label("VIRTUAL ENVIRONMENT", 10f, Color.rgb(141, 155, 176), true).apply {
            letterSpacing = 0.15f
        })
        header.addView(titles)
        root.addView(header)

        // Services chips
        val chips = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        playChip = chip(R.drawable.ic_play, "Google Play").apply { setOnClickListener { openGoogle(false) } }
        googleChip = chip(R.drawable.ic_games, "Play Games").apply { setOnClickListener { openGames() } }
        chips.addView(playChip)
        chips.addView(googleChip, LinearLayout.LayoutParams(-2, -2).apply { leftMargin = dp(10) })
        root.addView(chips, LinearLayout.LayoutParams(-2, -2).apply { topMargin = dp(22) })

        // App card
        val cardView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(18))
            background = rounded(card, 22, stroke)
        }
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        icon = ImageView(this).apply {
            setImageResource(android.R.drawable.sym_def_app_icon)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setPadding(dp(6), dp(6), dp(6), dp(6))
            background = rounded(Color.rgb(13, 18, 31), 18)
        }
        row.addView(icon, LinearLayout.LayoutParams(dp(76), dp(76)))

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), 0, 0, 0)
        }
        appName = label("Zooba", 19f, Color.WHITE, true)
        pkgLabel = label(targetPackage, 10f, dim)
        version = label("Version: —", 12f, dim)
        info.addView(appName)
        info.addView(pkgLabel, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(6) })
        info.addView(version, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(5) })
        row.addView(info, LinearLayout.LayoutParams(0, -2, 1f))
        cardView.addView(row)

        cardView.addView(View(this).apply { setBackgroundColor(stroke) },
            LinearLayout.LayoutParams(-1, dp(1)).apply { topMargin = dp(18) })

        val statusRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        statusDot = View(this).apply {
            background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(amber) }
        }
        statusRow.addView(statusDot, LinearLayout.LayoutParams(dp(9), dp(9)).apply {
            topMargin = dp(5)
            rightMargin = dp(10)
        })
        statusText = label("Checking installed application...", 12f, dim).apply {
            setLineSpacing(dp(3).toFloat(), 1f)
        }
        statusRow.addView(statusText, LinearLayout.LayoutParams(0, -2, 1f))
        cardView.addView(statusRow, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(16) })
        root.addView(cardView, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(22) })

        // Buttons
        importButton = actionButton("IMPORT ZOOBA", blue, violet)
        importButton.setOnClickListener { importZooba(true) }
        root.addView(importButton, LinearLayout.LayoutParams(-1, dp(56)).apply { topMargin = dp(26) })

        startButton = actionButton("START", Color.rgb(34, 197, 94), Color.rgb(22, 163, 74))
        startButton.isEnabled = false
        startButton.setOnClickListener {
            if (!ZuEngine.launch(targetPackage)) {
                setStatus(statusText.text.toString() + "\nLaunch failed", red)
            }
        }
        root.addView(startButton, LinearLayout.LayoutParams(-1, dp(56)).apply { topMargin = dp(12) })

        // Footer
        root.addView(label("INDEPENDENT VIRTUAL RUNTIME PROJECT", 10f, Color.rgb(101, 116, 139)).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.1f
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(34) })

        scroll.addView(root)
        setContentView(scroll)

        // Staggered entrance animation
        for (i in 0 until root.childCount) {
            val v = root.getChildAt(i)
            v.alpha = 0f
            v.translationY = dp(24).toFloat()
            v.animate().alpha(1f).translationY(0f)
                .setStartDelay(i * 70L).setDuration(380).start()
        }
    }

    private fun importZooba(force: Boolean) {
        val appInfo: ApplicationInfo = try {
            packageManager.getApplicationInfo(targetPackage, 0)
        } catch (e: Exception) {
            setStatus("Zooba is not installed on this phone.", red)
            return
        }
        val versionName = packageManager.getPackageInfo(targetPackage, 0).versionName ?: "Unknown"

        appName.text = appInfo.loadLabel(packageManager).toString()
        icon.setImageDrawable(packageManager.getApplicationIcon(appInfo))
        version.text = "Version: $versionName"

        val prefs = getSharedPreferences("zu", MODE_PRIVATE)
        val base = File(appDirectory, "base.apk")
        val splits = appInfo.splitSourceDirs ?: emptyArray()
        val needCopy = force || !base.exists() || !prefs.getBoolean("imported", false)

        importButton.isEnabled = false
        startButton.isEnabled = false
        setStatus(if (needCopy) "Importing APK files..." else "Checking virtual space...", amber)

        Thread {
            try {
                if (needCopy) {
                    appDirectory.mkdirs()
                    File(appInfo.sourceDir).copyTo(base, overwrite = true)
                    val splitDirectory = File(appDirectory, "splits")
                    splitDirectory.mkdirs()
                    splits.forEachIndexed { index, path ->
                        File(path).copyTo(File(splitDirectory, "split_$index.apk"), overwrite = true)
                    }
                    prefs.edit()
                        .putBoolean("imported", true)
                        .putString("package", targetPackage)
                        .putString("version", versionName)
                        .putInt("split_count", splits.size)
                        .apply()
                }
                val summary = "Base APK: saved\nSplit APKs: ${splits.size}"
                runOnUiThread {
                    setStatus("APK imported.\n$summary\nInstalling into virtual space...", amber)
                }

                val (ok, err) = if (ZuEngine.isInstalled(targetPackage)) Pair(true, "")
                else ZuEngine.install(targetPackage)

                runOnUiThread {
                    if (ok) {
                        setStatus("Ready.\n$summary\nVirtual install: OK", green)
                        startButton.isEnabled = true
                    } else {
                        setStatus("Virtual install failed: $err\n$summary", red)
                    }
                    importButton.isEnabled = true
                }
            } catch (e: Exception) {
                runOnUiThread {
                    setStatus("Import error: ${e.message ?: "unknown error"}", red)
                    importButton.isEnabled = true
                }
            }
        }.start()
    }

    private var playChip: LinearLayout? = null
    private var googleChip: LinearLayout? = null
    private var gmsBusy = false

    override fun onResume() {
        super.onResume()
        if (pendingLog) { pendingLog = false; copyLog() }
        refreshGms()
    }

    private fun refreshGms() {
        Thread {
            val ok = try { ZuEngine.isGmsInstalled() } catch (e: Throwable) { false }
            runOnUiThread {
                for (c in listOf(playChip, googleChip)) {
                    (c?.background as? GradientDrawable)?.setStroke(dp(1), if (ok) green else stroke)
                }
            }
        }.start()
    }

    private fun openGoogle(accountScreen: Boolean) {
        if (gmsBusy) return
        gmsBusy = true
        pendingLog = true
        playChip?.alpha = 0.5f
        googleChip?.alpha = 0.5f
        Thread {
            var ok = false
            var err = ""
            try {
                if (!ZuEngine.gmsSupported()) {
                    err = "Google services are not supported on this device"
                } else {
                    if (!ZuEngine.isGmsInstalled()) {
                        runOnUiThread { setStatus("Installing Google services into the virtual space...", amber) }
                        val r = ZuEngine.installGms()
                        if (!r.first) err = r.second
                    }
                    ok = err.isEmpty() && ZuEngine.isGmsInstalled()
                    if (!ok && err.isEmpty()) err = "installation did not complete"
                }
            } catch (e: Throwable) {
                err = e.message ?: "unknown error"
            }
            runOnUiThread {
                gmsBusy = false
                playChip?.alpha = 1f
                googleChip?.alpha = 1f
                if (!ok) {
                    setStatus("Google services failed: $err", red)
                } else {
                    refreshGms()
                    var opened = false
                    if (accountScreen) opened = ZuEngine.launch("com.google.android.gms")
                    if (!opened) opened = ZuEngine.launch("com.android.vending")
                    if (opened) {
                        setStatus("Google services ready.\nSign in or create your Google account in the window that opened.", green)
                    } else {
                        setStatus("Google services installed, but could not open Play Store.", amber)
                    }
                }
            }
        }.start()
    }

    private fun openGames() {
        if (gmsBusy) return
        gmsBusy = true
        pendingLog = true
        googleChip?.alpha = 0.5f
        val games = "com.google.android.play.games"
        Thread {
            var ok = false
            var err = ""
            try {
                val onPhone = try { packageManager.getPackageInfo(games, 0); true } catch (e: Exception) { false }
                if (!onPhone && !ZuEngine.isInstalled(games)) {
                    err = "Google Play Games is not installed on this phone. Install it from the Play Store on your phone, then tap again."
                } else {
                    if (!ZuEngine.isGmsInstalled()) {
                        runOnUiThread { setStatus("Installing Google services into the virtual space...", amber) }
                        val r = ZuEngine.installGms()
                        if (!r.first) err = r.second
                    }
                    if (err.isEmpty() && !ZuEngine.isInstalled(games)) {
                        runOnUiThread { setStatus("Installing Play Games into the virtual space...", amber) }
                        val r = ZuEngine.install(games)
                        if (!r.first) err = r.second
                    }
                }
                ok = err.isEmpty()
            } catch (e: Throwable) {
                err = e.message ?: "unknown error"
            }
            runOnUiThread {
                gmsBusy = false
                googleChip?.alpha = 1f
                if (!ok) {
                    setStatus("Play Games: $err", red)
                } else {
                    refreshGms()
                    if (ZuEngine.launch(games)) setStatus("Play Games opened.", green)
                    else setStatus("Play Games installed, but could not open it.", amber)
                }
            }
        }.start()
    }

    private var pendingLog = false

    private fun copyLog() {
        Thread {
            var msg: String
            try {
                val pr = ProcessBuilder("logcat", "-d", "-v", "threadtime").redirectErrorStream(true).start()
                val all = pr.inputStream.bufferedReader().readLines()
                File(filesDir, "zu_log.txt").writeText(all.joinToString("\n"))
                val rx = Regex("FATAL|AndroidRuntime|SIGSEGV|signal 11|Fatal signal|died|Caused by|Exception|UnsatisfiedLink|NoSuchMethod|ClassNotFound|vending|gms|blackbox", RegexOption.IGNORE_CASE)
                val fatal = all.indices.filter { all[it].contains("FATAL EXCEPTION") }.takeLast(3)
                val pick = mutableListOf<String>()
                for (i in fatal) {
                    pick.add(all[i])
                    var n = 0
                    for (j in i + 1 until minOf(all.size, i + 160)) {
                        val l = all[j]
                        if (!l.contains("AndroidRuntime")) continue
                        if (n < 4 || l.contains("Caused by") || l.contains("Exception:")) pick.add(l)
                        n++
                    }
                }
                if (pick.isEmpty()) pick.addAll(all.takeLast(60))
                val text = pick.joinToString("\n") { if (it.length > 230) it.substring(0, 230) else it }
                val cm = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                cm.setPrimaryClip(android.content.ClipData.newPlainText("zu_log", text))
                msg = "Log copied (${pick.size} lines). Paste it in the chat."
            } catch (e: Throwable) {
                msg = "Log copy failed: ${e.message}"
            }
            runOnUiThread {
                setStatus(msg, amber)
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
        }.start()
    }
}
