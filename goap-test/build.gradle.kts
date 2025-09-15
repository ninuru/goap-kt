plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":goap-core"))
    implementation(project(":goap-algorithms"))
    implementation(project(":goap-fsm"))

    implementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    implementation("org.junit.jupiter:junit-jupiter-engine:5.7.1")
    implementation("org.junit.platform:junit-platform-launcher:1.9.1")
}

tasks.test {
    useJUnitPlatform()
}
