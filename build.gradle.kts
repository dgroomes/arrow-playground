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
}

application {
    mainClass.set("dgroomes.Runner")
    mainModule.set("dgroomes")
}
