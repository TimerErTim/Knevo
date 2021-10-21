plugins {
    `java-library`

    kotlin("jvm") version "1.5.30"
    id("org.jetbrains.dokka") version "1.5.30"

    `maven-publish`
    signing
}

group = "eu.timerertim.knevo"
version = "0.1.0-RC2"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

val GPG_SIGNING_KEY: String? by project
val GPG_SIGNING_PASSWORD: String? by project

val MAVEN_UPLOAD_USER: String? by project
val MAVEN_UPLOAD_PWD: String? by project

tasks {
    register("sourcesJar", Jar::class) {
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    register("javadocJar", Jar::class) {
        dependsOn("dokkaJavadoc")
        archiveClassifier.set("javadoc")
        from(dokkaJavadoc)
    }
}

publishing {
    repositories {
        maven {
            name = "MavenCentral"
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
        create<MavenPublication>("core") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name.set("Knevo")
                description.set(
                    "Kotlin (and Java) Neuroevolution library featuring multiple algorithms, coroutines " +
                            "and serialization"
                )
                url.set("knevo.timerertim.eu")
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
    sign(publishing.publications["core"])
}
