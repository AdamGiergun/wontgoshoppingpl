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
        // mykotlinlibrary / myresources — publikowane lokalnie z projektu
        // ~/AndroidStudioProjects/mylibraries (`:<modul>:publishToMavenLocal`)
        mavenLocal()
        google()
        mavenCentral()
    }
}

rootProject.name = "wontgoshoppingpl"

include(":app")
