import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    
    jvm("desktop")
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.kotlinx.serialization.json)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.junit)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.core.ktx)
                implementation(libs.apache.poi)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.apache.poi)
            }
        }
    }
}

android {
    namespace = "com.mechi.personeltakip"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mechi.personeltakip"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)
            packageName = "MyMechiPersonelTakip"
            packageVersion = "1.2.0"
            description = "Mechi Kurumsal Personel Takip, Mesai & Bordro Otomasyonu"

            copyright = "© 2026 Mechi"
            vendor = "Mechi"
            
            windows {
                shortcut = true
                menu = true
                upgradeUuid = "d7e9b042-4f32-45e6-9ef7-8d32a93c12fe"
            }
        }
    }
}
