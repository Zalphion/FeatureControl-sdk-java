plugins {
    java
    alias(libs.plugins.lombok)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.shadow)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.jspecify)

    implementation(libs.argo)
    implementation(libs.slf4j.api)

    testCompileOnly(libs.jspecify)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.forkhandles.time4k)
    testImplementation(libs.assertj)

    testRuntimeOnly(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.slf4j.simple)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

sourceSets {
    create("examples") {
        java.srcDirs("src/examples/java")
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    }
}

val examplesImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
    extendsFrom(configurations.testImplementation.get())
}

tasks.shadowJar {
    relocate("argo", "com.zalphion.featurecontrol.lib.argo")
}