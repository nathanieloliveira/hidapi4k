plugins {
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
            implementation(libs.clikt)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(project(":hidapi-ffm"))
            implementation(project(":hidapi-natives", "native"))
        }
    }
}
