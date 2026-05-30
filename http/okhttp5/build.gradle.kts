dependencies {
    api(project(":core"))

    implementation(libs.okhttp5)

    compileOnly(libs.jspecify)

    testImplementation(testFixtures(project(":core")))
}