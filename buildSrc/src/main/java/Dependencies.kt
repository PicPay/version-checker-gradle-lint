object Versions {
    // Libs
    const val appCompatVersion = "1.0.0"
    const val retrofitVersion = "2.6.4"

    const val playServiceLocation = "16.0.0"

    const val koinCoreVersion = "2.0.1"

    // Test Libs
    const val junitTest = "4.12"
}

object Libs {

    val koinCore = "org.koin:koin-core:${Versions.koinCoreVersion}"
    val appCompat = "androidx.appcompat:appcompat:${Versions.appCompatVersion}" // AppCompat
    val retrofit =
        "com.squareup.retrofit2:retrofit:${Versions.retrofitVersion}"
    val playServiceLocation =
        "com.google.android.gms:play-services-location:${Versions.playServiceLocation}"
}

object TestLibs {

    val junit = "junit:junit:${Versions.junitTest}"
}
