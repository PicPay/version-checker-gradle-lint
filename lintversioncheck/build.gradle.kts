plugins {
    id("java-library")
    id("kotlin")
}

repositories {
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.71")
    implementation("com.android.tools.lint:lint-api:26.6.3")
    implementation("com.android.tools.lint:lint-checks:26.6.3")
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Manifest-Version"] =  1.0
        attributes["Lint-Registry"] =  "com.picpay.gradlelint.versioncheck.GradleVersionCheckerRegistry"
    }
}

tasks {
    register("copyLintJar", Copy::class) {
        description = "Copies the lint jar file into the {user.home}/.android/lint folder."
        from("build/libs/") {
            include("*.jar")
        }
        into(System.getProperty("user.home") + "/.android/lint")
    }
}


