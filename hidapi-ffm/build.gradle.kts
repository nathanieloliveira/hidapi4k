plugins {
    `maven-publish`
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(25)
        vendor = JvmVendorSpec.ADOPTIUM
    }
    jvm {

    }
    
    sourceSets {
        commonMain.dependencies {

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(libs.jna.core)
        }
    }
}

publishing {
    publications.withType<MavenPublication>()
}
