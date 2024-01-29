plugins {
    kotlin("jvm") version "1.9.22"
}

repositories {
    mavenCentral()
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}