plugins {
    java
    alias(libs.plugins.lombok)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.shadow)
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 8
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.jspecify)

    implementation(libs.argo)
    implementation(libs.slf4j.api)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.assertj)

    testRuntimeOnly(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.slf4j.simple)
}

configurations {
    testCompileOnly {
        extendsFrom(configurations.compileOnly.get())
    }
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
    // replace original JAR for maven distribution
    archiveClassifier.set("")

    // vendor argo, but move to a new package
    relocate("argo", "com.zalphion.featurecontrol.lib.argo")

    // don't vendor slf4j-api
    dependencies {
        exclude(dependency("org.slf4j:slf4j-api:.*"))
    }

    // include project license
    from(rootProject.file("LICENSE")) {
        into("")
    }

    // remove unused vendored code
    minimize()
}