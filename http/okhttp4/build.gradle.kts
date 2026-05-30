dependencies {
    api(project(":core"))

    implementation(libs.okhttp4)

    compileOnly(libs.jspecify)

    testImplementation(testFixtures(project(":core")))
}