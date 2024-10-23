plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.phishingblock"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.phishingblock"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "META-INF/*"
        }
    }
}


dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.preference)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // AndroidX 및 UI 라이브러리
    implementation("com.google.android.material:material:1.9.0")


    // FFmpeg 라이브러리
    implementation(libs.arthenica.mobile.ffmpeg.full.gpl)

// Google Cloud Speech API
    implementation("com.google.cloud:google-cloud-speech:1.30.0")

    // Google Cloud Storage API
    implementation("com.google.cloud:google-cloud-storage:2.13.0")

    // 테스트 관련 의존성
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")

    // gRPC 의존성 추가
    implementation("io.grpc:grpc-okhttp:1.56.0")
    implementation("io.grpc:grpc-auth:1.56.0")

    // CallCredentials 관련 gRPC 코어 의존성
    implementation("io.grpc:grpc-core:1.56.0")
    implementation("io.grpc:grpc-stub:1.56.0")
    implementation("io.grpc:grpc-protobuf:1.56.0")

    // 추가로 Netty 기반의 gRPC를 사용하고 싶다면 아래도 포함 가능
    implementation("io.grpc:grpc-netty:1.56.0")

    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    implementation("org.jsoup:jsoup:1.14.3")


    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")


}
