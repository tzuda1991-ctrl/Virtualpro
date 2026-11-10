package com.zu.virtual

import android.net.Uri
import top.niunaijun.blackbox.BlackBoxCore
import java.io.File

object ZuEngine {
    const val USER = 0

    fun isInstalled(pkg: String): Boolean =
        BlackBoxCore.get().isInstalled(pkg, USER)

    /** Clone an app already installed on the phone into the virtual space. */
    fun install(pkg: String): Pair<Boolean, String> {
        val r = BlackBoxCore.get().installPackageAsUser(pkg, USER)
        return Pair(r.success, r.msg ?: "")
    }

    /** Install an APK file into the virtual space. */
    fun installApk(file: File): Pair<Boolean, String> {
        val r = BlackBoxCore.get().installPackageAsUser(file, USER)
        return Pair(r.success, r.msg ?: "")
    }

    fun launch(pkg: String): Boolean =
        BlackBoxCore.get().launchApk(pkg, USER)

    fun uninstall(pkg: String) =
        BlackBoxCore.get().uninstallPackageAsUser(pkg, USER)

    fun gmsSupported(): Boolean = BlackBoxCore.get().isSupportGms()

    fun isGmsInstalled(): Boolean = BlackBoxCore.get().isInstallGms(USER)

    fun installGms(): Pair<Boolean, String> {
        val r = BlackBoxCore.get().installGms(USER)
        return Pair(r.success, r.msg ?: "")
    }
}
