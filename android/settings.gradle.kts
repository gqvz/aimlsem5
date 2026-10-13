pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("local-maven")
            content { includeModule("androidx.compose.material", "material-icons-extended-android") }
        }
        google()
        mavenCentral()
    }
}
rootProject.name = "Sahay"
include(":app")
