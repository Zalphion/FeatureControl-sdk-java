plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.forkhandles.result4k)

    implementation(libs.slf4j.api)
    implementation(libs.kotlin.serialization)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.forkhandles.result4k.kotest)
    testImplementation(libs.forkhandles.time4k)
    testImplementation(libs.kotest.assertions.core.jvm)

    testRuntimeOnly(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        allWarningsAsErrors = true
    }
}

tasks.test {
    useJUnitPlatform()
}

sourceSets {
    create("examples") {
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    }
}

val examplesImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
    extendsFrom(configurations.testImplementation.get())
}