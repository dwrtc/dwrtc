import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "ch.hsr.dsl.dwrtc"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.2.51"
    id("org.sonarqube") version "2.6"
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://tomp2p.net/dev/mvn/")
}

dependencies {
    compile(kotlin("stdlib-jdk8"))

    compile("net.tomp2p:tomp2p-all:5.0-Beta8")
    compile("io.javalin:javalin:2.2.0")
    compile("org.slf4j:slf4j-simple:1.7.25")

    testCompile("io.kotlintest:kotlintest-runner-junit5:3.1.8")
}

tasks.withType<Test> {
    useJUnitPlatform {}
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
