import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "ch.hsr.dsl.dwms"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.2.51"
    id("org.sonarqube") version "2.6"
}

repositories {
    mavenCentral()
    maven(url = "https://tomp2p.net/dev/mvn/")
}

dependencies {
    compile(kotlin("stdlib-jdk8"))

    compile("net.tomp2p:tomp2p-all:5.0-Beta8")

    testCompile("io.kotlintest:kotlintest-runner-junit5:3.1.8")
}

tasks.withType<Test> {
    useJUnitPlatform {}
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
