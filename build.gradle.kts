ext.apply {
    set("compileSdk", 34)
    set("compileSdkVersion", 34)
    set("targetSdk", 28)
    set("minSdk", 23)
    set("ktx_version", "1.12.0")
    set("stdlib_version", "1.8.22")
    set("googlematerial", "1.11.0")
    set("blackReflection", "1.1.4")
    set("appid", "com.zu.virtual")
    set("packagename", "com.zu.virtual")
    set("appversion", "1.0.0")
    set("versioncode", 1)
    set("versionCode", 1)
    set("versionName", "1.0.0")
}

plugins {
    id("com.android.application") version "8.3.0" apply false
    id("com.android.library") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}

tasks.register<Delete>("clean") { delete(rootProject.layout.buildDirectory) }
