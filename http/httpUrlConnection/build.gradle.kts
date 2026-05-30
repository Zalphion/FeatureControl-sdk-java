dependencies {
    api(project(":core"))

    compileOnly(libs.jspecify)

    testImplementation(testFixtures(project(":core")))
}
