plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.ngxyzaudax.radiacode.track"
    compileSdk {
        version = release(37)
    }
    defaultConfig {
        minSdk = 30
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":protocol"))
    implementation(libs.play.services.location)
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.core)
    testImplementation(libs.junit)
}
