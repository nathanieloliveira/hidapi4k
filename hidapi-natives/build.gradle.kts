import de.undercouch.gradle.tasks.download.Download
import org.gradle.internal.extensions.stdlib.capitalized

plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.gradleDownloadTask)
}

java {

}

val BUILD_VERSION = "0.15.11"
val NATIVES_VERSION = "0.15.0"
val BASE_URL =
    "https://github.com/nathanieloliveira/hidapi4k/releases/download/natives-$BUILD_VERSION"

data class PlatformNative(
    val os: String,
    val architecture: String,
    val download: String,
    val sourceName: String,
    val targetName: String,
)

val downloads = listOf(
    PlatformNative(
        "linux",
        "x86_64",
        "hidapi-ubuntu-24.04.so.zip",
        "libhidapi-hidraw.so.$NATIVES_VERSION",
        "libhidapi.so",
    ),
    PlatformNative(
        "linux",
        "aarch64",
        "hidapi-ubuntu-24.04-arm.so.zip",
        "libhidapi-hidraw.so.$NATIVES_VERSION",
        "libhidapi.so",
    ),
    PlatformNative(
        "windows",
        "x86_64",
        "hidapi-windows-latest.dll.zip",
        "hidapi.dll",
        "hidapi.dll",
    ),
    PlatformNative(
        "windows",
        "aarch64",
        "hidapi-windows-11-arm.dll.zip",
        "hidapi.dll",
        "hidapi.dll",
    ),
    PlatformNative(
        "macos",
        "x86_64",
        "hidapi-macos-x86_64.dylib.zip",
        "libhidapi.$NATIVES_VERSION.dylib",
        "libhidapi.dylib",
    ),
    PlatformNative(
        "macos",
        "aarch64",
        "hidapi-macos-latest.dylib.zip",
        "libhidapi.$NATIVES_VERSION.dylib",
        "libhidapi.dylib",
    ),
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

    val downloadFolder = layout.buildDirectory.dir("downloads/zip/$suffix")
    val unzipFolder = layout.buildDirectory.dir("downloads/$suffix")

    val downloadTask by tasks.register<Download>("download${suffix}") {
        src(downloadUrl)
        dest(downloadFolder)
        onlyIfModified(true)
    }

    val unzipTask by tasks.register<Copy>("unzip${suffix}") {
        dependsOn(downloadTask)
        from(zipTree(downloadTask.outputs.files.singleFile))
        into(layout.buildDirectory.dir("downloads/$suffix"))
    }

    val copyTask by tasks.register<Copy>("copy$suffix") {
        dependsOn(unzipTask)
        from(file(unzipFolder).resolve(plat.sourceName))
        into(layout.buildDirectory.dir("$resourceRootPath/hidapi4k/${plat.os}/${plat.architecture}"))
        rename { plat.targetName }
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
        dependsOn(copyTask)
    }

    tasks.named("assemble") {
        dependsOn(copyTask, jarTask)
    }

    artifacts {
        add("native", jarTask)
    }
}

// Fat JAR with all platform natives — this becomes the default (unclassified) artifact.
// Loader.kt uses hidapi4k/{os}/{arch}/{lib} path disambiguation, so all platforms coexist safely.
val fatNativesJar by tasks.register<Jar>("jarAllNatives") {
    archiveClassifier.set("")
    downloads.forEach { plat ->
        val suffix = "${plat.os.capitalized()}${plat.architecture.capitalized()}"
        dependsOn("copy${suffix}")
        from(layout.buildDirectory.dir("resources${suffix}"))
    }
}

tasks.named("assemble") {
    dependsOn(fatNativesJar)
}

publishing {
    publications {
        create<MavenPublication>("natives") {
            artifact(fatNativesJar)
            downloads.forEach { plat ->
                val suffix = "${plat.os.capitalized()}${plat.architecture.capitalized()}"
                artifact(tasks.named<Jar>("jar${suffix}"))
            }
        }
    }
}

