import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

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

    testImplementation(libs.assertj)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

application {
    mainClass.set("dgroomes.sortandsearch.Runner")
}

tasks {
    /**
     *  Configure tasks to enable Java language "Preview Features". Specifically, we want the "JEP 427: Pattern Matching
     *  for switch (Third Preview)" and "JEP 405: Record Patterns (Preview)" preview features.
     *
     *  https://openjdk.org/jeps/427
     *  https://openjdk.org/jeps/405
     */
    withType(JavaCompile::class.java) {
        options.compilerArgs.addAll(arrayOf("--enable-preview"))
    }

    withType<JavaExec> {
        jvmArgs = listOf(
            "--enable-preview",
            // Apache Arrow accesses internal Java modules reflectively. These modules need to be "opened" during
            // runtime.
            //
            // See https://arrow.apache.org/docs/java/install.html#java-compatibility
            "--add-opens=java.base/java.nio=ALL-UNNAMED",
        )
    }

    test {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
            events = setOf(TestLogEvent.STARTED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
        }

        jvmArgs = listOf(
            "--enable-preview",
            "--add-opens=java.base/java.nio=ALL-UNNAMED",
        )
    }
}
