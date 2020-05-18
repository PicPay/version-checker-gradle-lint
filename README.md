# Version Checker Gradle Lint
Warning on new versions available even when using Kotlin-DSL plugin.

![](example.png)

## `buildSrc` module with kotlin-dsl plugin
- `buildSrc/src/main/java/Dependencies.kt`
```kotlin
// file: Dependencies.kt

object Versions {

    val kotlinVersion = "1.3.70"
   
    val junit4Version = "4.12"
}

object Libs {

    val kotlin = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlinVersion}"
}

object TestLibs {

    val junit4 = "junit:junit:${Versions.junit4Version}"
}
```

## How install?
- Download ![jar](https://github.com/PicPay/version-checker-gradle-lint/releases/download/0.1.0/versioncheckerlint.jar), copy to `${USER_DIR}/.android/lint/` and enjoy!
