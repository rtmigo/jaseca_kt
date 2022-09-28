plugins {
    kotlin("jvm") version "1.7.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ehcache:ehcache:3.10.1")

    testImplementation(platform("org.junit:junit-bom:5.9.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.kotest:kotest-assertions-core:5.4.2")
}

tasks.test {
    useJUnitPlatform()
}