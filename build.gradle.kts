import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.2.51"
    id("org.sonarqube") version "2.6"
    id("java")
}

group = "ch.hsr.dsl.dwms"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "http://tomp2p.net/dev/mvn/")
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(group = "net.tomp2p", name = "tomp2pall", version = "5.0-Beta8")

}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}