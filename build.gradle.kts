plugins {
    java
    `java-test-fixtures`
    alias(libs.plugins.lombok) apply false
    alias(libs.plugins.maven.publish) apply false
}

allprojects {
    pluginManager.apply("java-test-fixtures")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(8)
        }
    }

    repositories {
        mavenCentral()
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

subprojects {
    pluginManager.apply("java-library")
    pluginManager.apply("maven-publish")
    pluginManager.apply("io.freefair.lombok")
}

dependencies {
    implementation(project(":core"))
    testImplementation(testFixtures(project(":core")))
}
