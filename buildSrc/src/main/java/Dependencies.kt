object Versions {
    // Libs
    const val appCompatVersion = "1.0.0"
    const val retrofitVersion = "2.6.4"

    const val playServicesVersion = "16.0.0"

    // Test Libs
    const val junitTest = "4.12"
}

object Libs {

    val appCompat = "androidx.appcompat:appcompat:${Versions.appCompatVersion}" // AppCompat
    val retrofit =
        "com.squareup.retrofit2:retrofit:${Versions.retrofitVersion}"
    val playServiceLocation =
        "com.google.android.gms:play-services-location:${Versions.playServicesVersion}"
}

object TestLibs {

    val junit = "junit:junit:${Versions.junitTest}"
}
