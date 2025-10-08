plugins {
    java
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.slf4j.api)
    implementation(libs.slf4j.simple)
    implementation(libs.arrow.vector)
    implementation(libs.arrow.memory.netty)
}

application {
    mainClass.set("Runner")

    applicationDefaultJvmArgs = listOf(
        // Apache Arrow accesses internal Java modules reflectively. These modules need to be "opened" during
        // runtime.
        //
        // See https://arrow.apache.org/docs/java/install.html#java-compatibility
        "--add-opens=java.base/java.nio=ALL-UNNAMED",
    )
}
