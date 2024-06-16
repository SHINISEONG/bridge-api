plugins {
    kotlin("jvm") version "1.9.23"
}

group = "io.practce.hss"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.1")
    implementation ("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}