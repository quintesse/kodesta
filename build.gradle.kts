import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.61"
    `maven-publish`
}

val mainClassName = "org.codejive.kodesta.cli.MainKt"

group = "org.codejive"
version = "1.0-SNAPSHOT"

java {
    withJavadocJar()
    withSourcesJar()
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.ajalt:clikt:2.1.0")
    implementation("com.beust:klaxon:5.0.13")
    implementation("org.yaml:snakeyaml:1.25")
    implementation("io.fabric8:maven-model-helper:13")

    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
    testImplementation("org.assertj:assertj-core:3.13.2")
    testImplementation("io.rest-assured:rest-assured:3.1.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = mainClassName
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

val fatJar = task("fatJar", type = Jar::class) {
    archiveClassifier.set("with-dependencies")
    manifest {
        attributes["Main-Class"] = mainClassName
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("Kodesta")
                description.set("A generic modular code generator")
                url.set("https://github.com/quintesse/kodesta")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("quintesse")
                        name.set("Tako Schotanus")
                        email.set("tako@codejive.org")
                    }
                }
                scm {
                    connection.set("scm:git:git@github.com:quintesse/kodesta.git")
                    developerConnection.set("scm:git:git@github.com:quintesse/kodesta.git")
                    url.set("https://github.com/quintesse/kodesta")
                }
            }
        }
    }
}
