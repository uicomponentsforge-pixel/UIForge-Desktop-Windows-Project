
plugins {
    kotlin("jvm") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("org.jetbrains.compose") version "1.7.3"
}

group = "com.uiforge"
version = "1.0.0"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
}

compose.desktop {
    application {
        mainClass = "com.uiforge.desktop.MainKt"

        buildTypes.release {
            proguard {
                isEnabled.set(false)
            }
        }

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi
            )

            packageName = "UIForge Desktop"
            packageVersion = "1.0.0"
            description = "A desktop-style workspace by UIForge"
            vendor = "UIForge"

            windows {
                menuGroup = "UIForge"
                upgradeUuid = "c7d4e9a8-5c31-4c5a-a5b0-8b3d7e2f6a10"
            }
        }
    }
}
