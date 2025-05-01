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
        // Mapbox Maven repository
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            // Credentials for downloading the Mapbox SDK
            credentials {
                // In settings.gradle.kts, use settings instead of project
                username = "mapbox"
                password = settings.extra.properties["MAPBOX_DOWNLOADS_TOKEN"] as String? ?: ""
            }
        }
    }
}

rootProject.name = "Auto Tawaf Counter"
include(":app")
