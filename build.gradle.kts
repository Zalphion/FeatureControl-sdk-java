plugins {
    java
    alias(libs.plugins.lombok)
    alias(libs.plugins.shadow)
//    alias(libs.plugins.maven.publish)
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

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.forkhandles.time4k)
    testImplementation("org.hamcrest:java-hamcrest:2.0.0.0")

    testRuntimeOnly(libs.junit.jupiter)
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
    // replace unshaded jar
    archiveClassifier.set("")
}