import de.undercouch.gradle.tasks.download.Download
import org.gradle.internal.extensions.stdlib.capitalized

plugins {
    `java-library`
    alias(libs.plugins.gradleDownloadTask)
}

java {

}

val NATIVES_VERSION = "0.15.0"
val BASE_URL = "https://github.com/nathanieloliveira/hidapi4k/releases/download/natives-$NATIVES_VERSION/"

data class PlatformNative(
    val os: String,
    val architecture: String,
    val download: String,
)

val downloads = listOf(
    PlatformNative("linux", "x86_64", "hidapi-ubuntu-24.04.so.zip"),
)

configurations {
    register("native") {
        extendsFrom(configurations["runtimeClasspath"])
    }
}

downloads.forEach { plat ->
    val downloadUrl = "$BASE_URL/${plat.download}"
    val suffix = "${plat.os.capitalized()}${plat.architecture.capitalized()}"
    val resourceRootPath = "resources${suffix}"

    val downloadTask by tasks.register<Download>("download${suffix}") {
        src(downloadUrl)
        dest(layout.buildDirectory.dir("downloads/$suffix"))
        onlyIfModified(true)
    }

    val unzipTask by tasks.register<Copy>("copy${suffix}") {
        dependsOn(downloadTask)
        from(zipTree(downloadTask.outputs.files.singleFile))
        into(layout.buildDirectory.dir("$resourceRootPath/hidapi4k/${plat.os}/${plat.architecture}"))
    }

    val srcSet by sourceSets.register(suffix) {
        this.resources.srcDir(layout.buildDirectory.dir(resourceRootPath))
    }

    val jarTask by tasks.register<Jar>("jar${suffix}") {
        archiveClassifier.set("${plat.os}-${plat.architecture}")
        from(srcSet.output)
        manifest {

        }
    }

    tasks.named("process${suffix}Resources") {
        dependsOn(unzipTask)
    }

    tasks.named("assemble") {
        dependsOn(jarTask)
    }

    artifacts {
        add("native", jarTask)
    }
}

