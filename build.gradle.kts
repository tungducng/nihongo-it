plugins {
    kotlin("jvm") version "1.9.25" apply false
    kotlin("plugin.spring") version "1.9.25" apply false
    kotlin("plugin.jpa") version "1.9.25" apply false
    id("org.springframework.boot") version "3.4.3" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.2.0" apply false
}

subprojects {
    group = "com.example"
    version = "0.0.1-SNAPSHOT"
    
    repositories {
        mavenCentral()
    }
}

tasks.register("clean") {
    delete(rootProject.buildDir)
} 