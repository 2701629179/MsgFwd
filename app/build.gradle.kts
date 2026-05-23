plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

android {
  namespace = "com.phonemonitor"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.xiaoxiforwarder"
    minSdk = 21
    targetSdk = 34
    versionCode = 3
    versionName = "3.0"
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    debug {
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions { jvmTarget = "17" }
  buildFeatures { compose = true }
  composeOptions { kotlinCompilerExtensionVersion = "1.5.5" }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
      excludes += "/META-INF/DEPENDENCIES"
      excludes += "/META-INF/LICENSE"
      excludes += "/META-INF/LICENSE.txt"
      excludes += "/META-INF/NOTICE"
      excludes += "/META-INF/NOTICE.txt"
      excludes += "/META-INF/*.SF"
      excludes += "/META-INF/*.RSA"
      excludes += "/META-INF/*.md"
    }
  }
}

dependencies {
  val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
  implementation(composeBom)
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.material:material-icons-extended")
  implementation("androidx.activity:activity-compose:1.8.1")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
  implementation("androidx.core:core-ktx:1.12.0")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
