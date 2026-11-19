plugins { id("com.android.application") }

android {
    namespace = "com.ghostlock.ksudstub"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ghostlock.ksudstub"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release { isMinifyEnabled = false }
    }

    packaging {
        jniLibs { useLegacyPackaging = true }
    }
}
