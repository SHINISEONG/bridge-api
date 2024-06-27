plugins {
    kotlin("jvm") version "1.9.23"
    `maven-publish`
    `java-library`
}

group = "io.github.shiniseong.bridge-api"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
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
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "io.hss.bridge-api"
            artifactId = "bridge-api"
            version = "1.0.1"

            pom {
                name.set("Bridge API")
                description.set("A library to call Kotlin code via JSBridge in a REST API style familiar to web developers.")
                url.set("https://github.com/SHINISEONG/bridge-api")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("SHINISEONG")
                        name.set("HEESEONG SHIN")
                        email.set("hss275989@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/SHINISEONG/bridge-api.git")
                    developerConnection.set("scm:git:ssh://github.com/SHINISEONG/bridge-api.git")
                    url.set("https://github.com/SHINISEONG/bridge-api")
                }
            }
        }
    }

    repositories {
        maven {
            name = "sonatype"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")

            credentials {
                username = project.findProperty("ossrhUsername") as String? ?: ""
                password = project.findProperty("ossrhPassword") as String? ?: ""
            }
        }
    }
}