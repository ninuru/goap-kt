plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":goap-core"))
    api(project(":goap-algorithms"))
    api(project(":goap-fsm"))

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.0")
}

tasks.test {
    useJUnitPlatform()
}
