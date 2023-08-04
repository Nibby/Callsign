plugins {
    id("codes.nibby.sherlock.java-application-conventions")
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

application {
    mainClass.set("codes.nibby.sherlock.viewer.ViewerApplication")
}