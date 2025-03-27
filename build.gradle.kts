plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.example.gitrenamecommit"
version = "1.0.0"

repositories {
    mavenCentral()
}

intellij {
    version.set("2023.3")
    type.set("IC")
    plugins.set(listOf("Git4Idea"))
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

tasks {
    
    test {
        useJUnit()
        
        testLogging {
            events("passed", "skipped", "failed")
            showExceptions = true
            showCauses = true
            showStackTraces = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
}

kotlin {
    jvmToolchain(17)
}
