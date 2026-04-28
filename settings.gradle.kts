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
        google()
        mavenCentral()
    }
}

rootProject.name = "RichEditor"

include(
    ":app",
    ":core:core-common",
    ":core:core-design",
    ":core:core-ui",
    ":core:core-database",
    ":core:core-datastore",
    ":core:core-media",
    ":core:core-markdown",
    ":feature:feature-note-list",
    ":feature:feature-note-detail",
    ":feature:feature-note-editor",
    ":feature:feature-settings",
    ":data:data-note",
)
