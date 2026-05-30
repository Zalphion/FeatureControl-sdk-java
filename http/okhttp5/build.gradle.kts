dependencies {
    api(project(":sdk"))

    implementation(libs.okhttp5)

    compileOnly(libs.jspecify)

    testImplementation(testFixtures(project(":sdk")))
}