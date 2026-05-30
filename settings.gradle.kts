rootProject.name = "FeatureControl-sdk-java"

include(":sdk")

include(":http-okhttp4", ":http-okhttp5", ":http-java8")

project(":http-java8").projectDir = file("http/java8")
project(":http-okhttp4").projectDir = file("http/okhttp4")
project(":http-okhttp5").projectDir = file("http/okhttp5")