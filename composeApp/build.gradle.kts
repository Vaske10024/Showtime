import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val isMacOs = System.getProperty("os.name").startsWith("Mac", ignoreCase = true)

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.ktorfit)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    if (isMacOs) {
        listOf(
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "ComposeApp"
                isStatic = true
            }
        }
    }
    
    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)

            // Ktor
            implementation(libs.ktor.client.okhttp)

            // Room
            implementation(libs.room.runtime.android)
        }
        commonMain.dependencies {
            // Compose Multiplatform
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.backhandler)
            implementation(libs.compose.foundation)
            implementation(libs.compose.animation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.material3.adaptive.navigation.suite)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)

            // Lifecycle & Navigation
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)

            // Ktor & Serialization
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.kotlinx.serialization.json)

            // Ktorfit
            implementation(libs.ktorfit.lib)
            implementation(libs.ktorfit.converters.response)
            implementation(libs.ktorfit.converters.call)
            implementation(libs.ktorfit.converters.flow)

            // DateTime
            implementation(libs.kotlinx.datetime)

            // Room
            implementation(libs.room.runtime)
            implementation(libs.room.paging)
            implementation(libs.sqlite.bundled)

            // Paging
            implementation(libs.paging.common)
            implementation(libs.paging.compose)

            // Image Loading
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)

            // Logging
            implementation(libs.napier)

            // Adaptive UI
            implementation(libs.adaptive.ui)

            // DataStore KMP with Okio
            implementation(libs.datastore.core)
            implementation(libs.datastore.core.okio)
            implementation(libs.okio)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.cio)
        }
        if (isMacOs) {
            named("iosMain") {
                dependencies {
                    implementation(libs.ktor.client.darwin)
                }
            }
        }
    }
}

android {
    namespace = "rs.edu.raf.rma"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
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

ktorfit {
    compilerPluginVersion.set("2.3.3")
}

room {
    schemaDirectory("$projectDir/schemas")
}

tasks.matching { it.name == "kmpPartiallyResolvedDependenciesChecker" }.configureEach {
    enabled = false
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
    val kspTargets = mutableListOf(
        "kspAndroid",
        "kspJvm"
    )
    if (isMacOs) {
        kspTargets += listOf(
            "kspIosSimulatorArm64",
            "kspIosArm64"
        )
    }
    kspTargets.forEach {
        add(it, libs.room.compiler)
    }
}
