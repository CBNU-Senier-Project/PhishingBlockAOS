pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google() // Google Maven repository
        mavenCentral() // Maven Central repository
        maven { url =uri("https://www.jitpack.io") } // JitPack for mobile-ffmpeg
    }

}

rootProject.name = "phishing block"
include(":app")
 