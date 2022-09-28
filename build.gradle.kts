plugins {
    kotlin("jvm") // version "1.7.10"
    id("java-library")
    java
}

repositories {
    mavenCentral()
}

group = "io.github.rtmigo"
version = "0.0-SNAPSHOT"

dependencies {
    implementation("org.ehcache:ehcache:3.10.1")

    testImplementation(platform("org.junit:junit-bom:5.9.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.kotest:kotest-assertions-core:5.4.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register("pkgver") {
    doLast {
        println(project.version.toString())
    }
}