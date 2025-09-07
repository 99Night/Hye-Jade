import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") // ✅ 반드시 추가
    id("io.objectbox")
}


android {
    namespace = "com.example.hyejade"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.hyejade"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a")  // ✅ Kotlin DSL 방식
        }
        packaging {
            jniLibs {
                useLegacyPackaging = true  // ✅ 반드시 '=' 사용
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-Xjvm-default=all"
            )
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}
// Kotlin 컴파일러 툴체인도 17
kotlin {
    jvmToolchain(17)
}
dependencies {
    implementation("io.objectbox:objectbox-android:3.7.1") // 최신 안정 버전

    implementation("com.google.mediapipe:tasks-text:latest.release")
    implementation("com.google.mediapipe:tasks-core:0.10.14")
    implementation("com.google.mediapipe:tasks-text:0.10.14")

    implementation("com.orhanobut:logger:2.2.0")
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // 필수 컴포넌트
    implementation("androidx.activity:activity-compose") // setContent 등
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose") // viewModel()

    // 선택적
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material:material-icons-extended")

    // 테스트/디버그
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation(group = "org.bsc.langgraph4j", name = "langgraph4j-studio", version = "1.6.0")
    implementation(group = "org.bsc.langgraph4j", name = "langgraph4j-bom", version = "1.6.0")
    implementation(
        group = "org.bsc.langgraph4j",
        name = "langgraph4j-adaptive-rag",
        version = "1.5.12"
    )
    implementation(group = "org.bsc.langgraph4j", name = "langgraph4j-core", version = "1.6.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

apply(plugin = "io.objectbox")
