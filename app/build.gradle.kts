plugins {
    alias(libs.plugins.android.application)
<<<<<<< HEAD
}

android {
    namespace = "com.example.lottieanimation"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.lottieanimation"
=======
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.sign_in"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sign_in"
>>>>>>> 544a59e (files added)
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
<<<<<<< HEAD
=======
    buildFeatures{
        viewBinding = true
    }
>>>>>>> 544a59e (files added)
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
<<<<<<< HEAD
    implementation("com.airbnb.android:lottie:6.4.0")
=======
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
>>>>>>> 544a59e (files added)
}