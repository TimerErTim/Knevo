plugins {
    kotlin("multiplatform") version "1.6.0"
    kotlin("plugin.serialization") version "1.6.0"
    id("org.jetbrains.dokka") version "1.6.0"

    `maven-publish`
    signing
}

group = "eu.timerertim.knevo"
version = "0.2.0-RC"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        withJava()
    }
    js(BOTH) {
        nodejs()
        browser()
    }
    wasm32()
    ios()
    watchos()
    tvos()
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()
    mingwX86()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-RC")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

val GPG_SIGNING_KEY: String? by project
val GPG_SIGNING_PASSWORD: String? by project

val MAVEN_UPLOAD_USER: String? by project
val MAVEN_UPLOAD_PWD: String? by project

tasks {
    register("javadocJar", Jar::class) {
        dependsOn("dokkaJavadoc")
        archiveClassifier.set("javadoc")
        from(dokkaJavadoc)
    }
}

publishing {
    repositories {
        maven {
            name = "Sonatype"
            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
                username = MAVEN_UPLOAD_USER
                password = MAVEN_UPLOAD_PWD
            }
        }
    }
    publications {
        withType<MavenPublication> {
            artifact(tasks["javadocJar"])

            pom {
                name.set("Knevo")
                description.set(
                    "Kotlin (and Java) Neuroevolution library featuring multiple algorithms, coroutines " +
                            "and serialization"
                )
                url.set("https://github.com/TimerErTim/Knevo")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://raw.githubusercontent.com/TimerErTim/Knevo/master/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("timerertim")
                        name.set("Tim Peko")
                        email.set("timerertim@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/TimerErTim/Knevo.git")
                    developerConnection.set("scm:git:https://github.com/TimerErTim/Knevo.git")
                    url.set("https://github.com/TimerErTim/Knevo")
                }
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(GPG_SIGNING_KEY, GPG_SIGNING_PASSWORD)
    sign(publishing.publications)
}
