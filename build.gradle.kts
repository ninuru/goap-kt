plugins {
    kotlin("jvm") version "2.1.20" apply false
}

allprojects {
    group = "io.github.ninuru.goap"
    version = "0.1.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "kotlin")

    tasks.withType<JavaCompile> {
        options.release.set(17)
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
