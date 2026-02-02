plugins {

    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "com.neatroots.newdog"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.neatroots.newdog"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.5.0"))
    implementation("com.google.firebase:firebase-storage-ktx:21.0.1")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.2")


    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.9.2")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")

    implementation("com.google.android.libraries.places:places:4.1.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout-core:1.1.1")
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    dependencies {
        implementation("androidx.core:core-ktx:1.12.0")
        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("com.google.android.material:material:1.11.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
        implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
        implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
        implementation("com.google.firebase:firebase-auth:22.3.1")
        implementation("com.google.firebase:firebase-database-ktx:20.3.0")
        implementation("com.google.firebase:firebase-firestore:24.10.2")
        implementation("com.github.CanHub:Android-Image-Cropper:4.2.0")

        implementation("com.squareup.picasso:picasso:2.71828")
        implementation("com.github.bumptech.glide:glide:4.12.0")
        kapt("com.github.bumptech.glide:compiler:4.12.0")
        implementation("androidx.fragment:fragment-ktx:1.3.6")
        implementation("de.hdodenhof:circleimageview:3.1.0")
        implementation ("com.squareup.picasso:picasso:2.71828")




        // Room Database
        val room_version = "2.6.1"
        implementation("androidx.room:room-runtime:$room_version")
        kapt("androidx.room:room-compiler:$room_version")

        // Unit testing and Android testing
        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

        //
        implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.29")

        implementation ("com.github.marlonlom:timeago:4.0.3")

        implementation ("com.prolificinteractive:material-calendarview:1.4.3")

        implementation ("com.github.sundeepk:compact-calendar-view:3.0.0")
        implementation("com.kizitonwose.calendar:compose:2.4.0")

        implementation ("com.google.firebase:firebase-appcheck-playintegrity:16.1.2")


    }

}
