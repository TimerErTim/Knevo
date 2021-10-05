import java.util.*
import javax.crypto.spec.SecretKeySpec

plugins {
    `java-library`

    kotlin("jvm") version "1.5.30"
    id("org.jetbrains.dokka") version "1.5.30"

    `maven-publish`
    signing
}

group = "eu.timerertim.knevo"
version = "0.1.0-alpha"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

val AES_ENCRYPTED_KEY =
    "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"
val AES_SECRET_KEY: String? by project
val GPG_SIGNING_PASSWORD: String? by project

val MAVEN_UPLOAD_USER: String? by project
val MAVEN_UPLOAD_PWD: String? by project

tasks {
    val sourcesJar by registering(Jar::class) {
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    val javadocJar by registering(Jar::class) {
        dependsOn("dokkaJavadoc")
        archiveClassifier.set("javadoc")
        from(javadoc)
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
            artifact(tasks["jar"])
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
    fun cipher(opmode: Int, secretKey: String): javax.crypto.Cipher {
        if (secretKey.length != 32) throw RuntimeException("SecretKey length is not 32 chars")
        val c = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding")
        val sk = SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), "AES")
        val iv = javax.crypto.spec.IvParameterSpec(secretKey.substring(0, 16).toByteArray(Charsets.UTF_8))
        c.init(opmode, sk, iv)
        return c
    }

    fun decrypt(str: String, secretKey: String?): String? {
        secretKey ?: return null
        val byteStr = Base64.getDecoder().decode(str.toByteArray(Charsets.UTF_8))
        return String(cipher(javax.crypto.Cipher.DECRYPT_MODE, secretKey).doFinal(byteStr))
    }

    val GPG_SIGNING_KEY = decrypt(AES_ENCRYPTED_KEY, AES_SECRET_KEY)
    useInMemoryPgpKeys(GPG_SIGNING_KEY, GPG_SIGNING_PASSWORD)
    sign(publishing.publications["core"])
}
