dependencies {
    api(project(":sdk"))

    compileOnly(libs.jspecify)

    testImplementation(testFixtures(project(":sdk")))
}
