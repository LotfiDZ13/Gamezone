pluginManagement {
    repositories {
        google() // 🟢 This is required for com.google.gms.google-services
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") } // 🟢 JitPack for FFmpeg
    }
}

rootProject.name = "gamezone"
include(":app")