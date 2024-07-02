plugins {
    kotlin("jvm") version "1.9.23"
    `maven-publish`
}

group = "io.github.shiniseong.bridge-api"
version = "1.1.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-core:1.5.6")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create<MavenPublication>("mavenKotlin") {
            from(components["kotlin"])

            groupId = "io.github.shiniseong.bridge-api"
            artifactId = "bridge-api"
            version = "1.1.1"

            pom {
                name.set("Bridge API")
                description.set("Bridge API is a simple library that helps facilitate communication between JavaScript and Android using REST API methods.")
                url.set("https://github.com/shiniseong/bridge-api")

                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("shiniseong")
                        name.set("HEESEONG SHIN")
                        email.set("hss275989@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/shiniseong/bridge-api.git")
                    developerConnection.set("scm:git:ssh://github.com/shiniseong/bridge-api.git")
                    url.set("https://github.com/shiniseong/bridge-api")
                }
            }
        }
    }
}