# Version Checker Gradle Lint
Warning on new versions available even when using Kotlin-DSL plugin.

![](example.png)

## How to use?
- Add lint dependency:
> copy `version-checker.jar` to `HOME/.android/lint/`

- Enable/Disable lint with `lintOptions` (default: `enabled`)
```groovy
lintOptions {
    enable "VersionCheckerGradleLint"
}
```

## `buildSrc` module with kotlin-dsl plugin

### Create `version` file
```kotlin
object Versions {

    val kotlinVersion = "1.3.70"
   
    val junit4Version = "4.12"
}
```
### Create lib files
```kotlin
object Libs {

    val kotlin = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlinVersion}"
}
```

```kotlin
object TestLibs {

    val junit4 = "junit:junit:${Versions.junit4Version}"
}
```

```kotlin
object OtherLibs {

    val myLib = "mylib:mylib:${Versions.myLib}"
}
```