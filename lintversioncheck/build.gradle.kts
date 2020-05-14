plugins {
    id("java-library")
    id("kotlin")
    id("jacoco")
}

repositories {
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.72")
    implementation("com.android.tools.lint:lint-api:26.6.3")
    implementation("com.android.tools.lint:lint-checks:26.6.3")
    testImplementation("junit:junit:4.13")
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Manifest-Version"] =  1.0
        attributes["Lint-Registry"] =  "com.picpay.gradlelint.versioncheck.GradleVersionCheckerRegistry"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = mutableListOf("-Xopt-in=kotlin.RequiresOptIn").apply {
            if (freeCompilerArgs.isNotEmpty()) addAll(freeCompilerArgs)
        }
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

tasks.test {
    finalizedBy("jacocoTestReport")
    doLast {
        println("View code coverage at:")
        println("file://$buildDir/reports/jacoco/test/html/index.html")
    }
}


