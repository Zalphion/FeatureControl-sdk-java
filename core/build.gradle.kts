plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    compileOnly(libs.jspecify)

    implementation(libs.argo)
    implementation(libs.slf4j.api)

    testFixturesApi(libs.junit.jupiter.api)
    testFixturesApi(libs.assertj)

    testFixturesRuntimeOnly(libs.junit.jupiter)
    testFixturesRuntimeOnly(libs.junit.platform.launcher)
    testFixturesRuntimeOnly(libs.slf4j.simple)
}

configurations {
    testFixturesApi {
        extendsFrom(configurations.compileOnly.get())
    }
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