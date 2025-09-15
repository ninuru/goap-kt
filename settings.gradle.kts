pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        kotlin("jvm") version "2.1.20"
    }
}

rootProject.name = "goap-kt"

include(
    "goap-core",
    "goap-algorithms",
    "goap-fsm",
    "goap-dsl",
    "goap-test",
    "goap-aliases"
)
