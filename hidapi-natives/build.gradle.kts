import de.undercouch.gradle.tasks.download.Download

plugins {
    `java-library`
    alias(libs.plugins.gradleDownloadTask)
}

java {

}

val downloads = mapOf(
    "WindowsX86_64" to "https://github.com/nathanieloliveira/hidapi4k/actions/runs/21366570135/artifacts/5260177584",
    "WindowsArm64" to "",
    "LinuxX86_64" to "https://github.com/nathanieloliveira/hidapi4k/actions/runs/21366570135/artifacts/5260173620",
    "LinuxArm64" to "",
)

downloads.forEach { (name, url) ->
    if (url.isBlank()) {
        return@forEach
    }

    val downloadTask by tasks.register<Download>("download${name.capitalize()}") {
        src(url)
        dest(layout.buildDirectory.dir("downloads/$name"))
        onlyIfModified(true)
    }

    val unzipTask by tasks.register<Copy>("copy${name.capitalize()}") {
        zipTree(downloadTask.dest)
        into(layout.buildDirectory.dir("resources/$name"))
    }

    tasks.named("processResources") {
        dependsOn(unzipTask)
    }
}

