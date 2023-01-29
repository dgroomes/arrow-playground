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
    implementation(platform(libs.jackson.bom))

    implementation(libs.arrow.vector)
    implementation(libs.arrow.algorithm)
    implementation(libs.arrow.memory.netty)
    implementation(libs.jackson.databind)
}

application {
    mainClass.set("dgroomes.sortandsearch.Runner")
}

tasks {
    withType<JavaExec> {
        jvmArgs = listOf(
            // Apache Arrow accesses internal Java modules reflectively. These modules need to be "opened" during
            // runtime.
            //
            // See https://arrow.apache.org/docs/java/install.html#java-compatibility
            "--add-opens=java.base/java.nio=ALL-UNNAMED",
        )
    }
}
