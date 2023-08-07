plugins {
    id("codes.nibby.callsign.java-application-conventions")
    id("org.openjfx.javafxplugin") version "0.0.14"
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")
    implementation("com.google.guava:guava:32.1.2-jre")

    implementation(project(":api:kotlin"))
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

javafx {
    version = "20"
    modules("javafx.controls")
}

application {
    mainClass.set("codes.nibby.callsign.viewer.ViewerApplication")
}