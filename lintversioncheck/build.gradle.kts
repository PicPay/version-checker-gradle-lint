import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("java-library")
    id("kotlin")
    id("jacoco")
    id("maven-publish")
}

repositories {
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.10")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.10")
    implementation("com.android.tools.lint:lint-api:27.0.2")
    implementation("com.android.tools.lint:lint-checks:27.0.2")
    testImplementation("junit:junit:4.13")
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Manifest-Version"] = 1.0
        attributes["Lint-Registry"] =
            "com.picpay.gradlelint.versioncheck.VersionCheckerGradleLintRegistry"
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

    register("coverageReport", JacocoReport::class) {
        group = "verification"
        sourceDirectories.from("$projectDir/src/main/java")
        classDirectories.from("$buildDir/classes/kotlin/main")
        reports {
            xml.isEnabled = true
            html.isEnabled = true

            xml.destination = file("$buildDir/jacoco/coverage.xml")
        }
        executionData.from("$buildDir/jacoco/test.exec")
    }
}

publishing {
    publications {
        register("publishArtifact", MavenPublication::class) {
            from(components["java"])
            groupId = project.findProperty("library.groupId") as String
            artifactId = project.findProperty("library.artifactId") as String
            version = (project.findProperty("library.version") as String) + versionSuffix()
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            val repository = project.findProperty("library.repository")
            url = uri("https://maven.pkg.github.com/PicPay/$repository")
            credentials {
                username = System.getenv("GITHUB_USER")
                password = System.getenv("GITHUB_USER_TOKEN")
            }
        }
    }
}

fun versionSuffix(): String {
    val sufix = System.getenv("VERSION_SUFIX").orEmpty()
    if (sufix.isNotEmpty()) {
        return "$sufix-${SimpleDateFormat("yyyyMMddHHmmss").format(Date())}"
    }
    return sufix
}
