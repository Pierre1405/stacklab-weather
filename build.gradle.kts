import io.github.robwin.jgitflow.tasks.InitJGitflowTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    kotlin("jvm") version "1.9.25" apply false
    id("net.researchgate.release") version "3.0.2"
    id("org.hidetake.swagger.generator") version "2.19.2"
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("io.github.robwin:jgitflow-gradle-plugin:0.6.0")
        classpath("org.openapitools:openapi-generator-gradle-plugin:7.8.0")
    }
}

apply(plugin= "io.github.robwin.jgitflow")
tasks.named(name = "initJGitflow", type = InitJGitflowTask::class) {
    master = "main"
}

apply(plugin = "org.openapi.generator")
tasks.named(name = "openApiGenerate", GenerateTask::class) {
    inputSpec = "$rootDir/swagger/weatherbit/swagger.json"
    outputDir = "$rootDir/weatherbit-client"
    generatorName = "kotlin"
    library = "jvm-spring-restclient"
    packageName = "com.stacklabs.weather.weatherbit"
    groupId = "org.stacklabs"
    id = "weatherbit-client"
    version = getVersion().toString()
    skipOperationExample = true
    generateModelTests = false
    generateModelDocumentation = false
    generateApiTests = false
    generateApiDocumentation = false
    cleanupOutput = true

    additionalProperties = mapOf(
        "omitGradlePluginVersions" to true,
        "useSpringBoot3" to true,
        "serializationLibrary" to "jackson",
        "useSettingsGradle" to false,
        "omitGradleWrapper" to true
    )
}

tasks.register("printVersionInformation", DefaultTask::class) {
    doLast {
        println(getVersion().toString())
    }
}