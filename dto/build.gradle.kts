plugins {
    kotlin("jvm")
}

group = "org.stacklabs"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("io.swagger.core.v3:swagger-annotations-jakarta:2.2.9")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.18.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}