plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.ngxyzaudax.radiacode.watchface"
    compileSdk {
        version = release(37)
    }
    defaultConfig {
        applicationId = "com.ngxyzaudax.radiacode.watchface"
        minSdk = 34
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
}
