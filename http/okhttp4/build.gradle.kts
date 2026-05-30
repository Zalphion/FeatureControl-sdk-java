dependencies {
    api(project(":sdk"))

    implementation(libs.okhttp4)

    compileOnly(libs.jspecify)

    testImplementation(testFixtures(project(":sdk")))
}