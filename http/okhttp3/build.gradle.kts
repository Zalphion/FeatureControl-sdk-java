dependencies {
    api(project(":core"))

    implementation(libs.okhttp3)

    compileOnly(libs.jspecify)

    testImplementation(testFixtures(project(":core")))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}