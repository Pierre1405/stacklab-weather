import io.github.robwin.jgitflow.tasks.InitJGitflowTask

plugins {
    id("net.researchgate.release") version "3.0.2"
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("io.github.robwin:jgitflow-gradle-plugin:0.6.0")
    }
}

apply(plugin= "io.github.robwin.jgitflow")
tasks.named(name = "initJGitflow", type = InitJGitflowTask::class) {
    master = "main"
}