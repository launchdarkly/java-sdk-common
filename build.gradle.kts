import java.time.Duration
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.external.javadoc.CoreJavadocOptions

// These values come from gradle.properties
val ossrhUsername: String by project
val ossrhPassword: String by project

buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    java
    "java-library"
    checkstyle
    signing
    "maven-publish"
    idea
    jacoco
    id("de.marcphilipp.nexus-publish") version "0.3.0"
    id("io.codearte.nexus-staging") version "0.30.0"
}

repositories {
    mavenLocal()
    // Before LaunchDarkly release artifacts get synced to Maven Central they are here along with snapshots:
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    mavenCentral()
}

apply { from("build-shared.gradle") }

java {
    withJavadocJar()
    withSourcesJar()
}

checkstyle {
    configFile = file("${project.rootDir}/checkstyle.xml")
}

tasks.compileJava {
    // See note in build-shared.gradle on the purpose of "privateImplementation"
    classpath = configurations["privateImplementation"]
}

tasks.javadoc.configure {
    // Force the Javadoc build to fail if there are any Javadoc warnings. See: https://discuss.gradle.org/t/javadoc-fail-on-warning/18141/3
    // See JDK-8200363 (https://bugs.openjdk.java.net/browse/JDK-8200363)
    // for information about the -Xwerror option.
    (options as CoreJavadocOptions).addStringOption("Xwerror")

    // See note in build-shared.gradle on the purpose of "privateImplementation"
    classpath = configurations["privateImplementation"]
}

tasks.test.configure {
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.jacocoTestReport.configure {
    reports {
        xml.isEnabled = true
        csv.isEnabled = true
        html.isEnabled = true
    }
}

tasks.jacocoTestCoverageVerification.configure {
    // See notes in CONTRIBUTING.md on code coverage. Unfortunately we can't configure line-by-line code
    // coverage overrides within the source code itself, because Jacoco operates on bytecode.
    violationRules {
        val knownMissedLinesForMethods = mapOf(
            "EvaluationReason.error(com.launchdarkly.sdk.EvaluationReason.ErrorKind)" to 1,
            "EvaluationReasonTypeAdapter.parse(com.google.gson.stream.JsonReader)" to 1,
            "LDValue.equals(java.lang.Object)" to 1,
            "LDValueTypeAdapter.read(com.google.gson.stream.JsonReader)" to 1,
            "json.JsonSerialization.getDeserializableClasses()" to -1,
            "json.LDGson.LDTypeAdapter.write(com.google.gson.stream.JsonWriter, java.lang.Object)" to 1,
            "json.LDJackson.GsonReaderToJacksonParserAdapter.peekInternal()" to 3
        )

        knownMissedLinesForMethods.forEach { (signature, maxMissedLines) ->
            if (maxMissedLines > 0) {  // < 0 means skip entire method
                rule {
                    element = "METHOD"
                    includes = listOf("com.launchdarkly.sdk." + signature)
                    limit {
                        counter = "LINE"
                        value = "MISSEDCOUNT"
                        maximum = maxMissedLines.toBigDecimal()
                    }
                }
            }
        }
        
        // General rule that we should expect 100% test coverage; exclude any methods that have overrides above
        rule {
            element = "METHOD"
            limit {
                counter = "LINE"
                value = "MISSEDCOUNT"
                maximum = 0.toBigDecimal()
            }
            excludes = knownMissedLinesForMethods.map { (signature, maxMissedLines) ->
                "com.launchdarkly.sdk." + signature }
        }
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

nexusStaging {
    packageGroup = "com.launchdarkly"
    numberOfRetries = 40 // we've seen extremely long delays in closing repositories
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "com.launchdarkly"
            artifactId = "launchdarkly-java-sdk-common"

            pom {
                name.set("launchdarkly-java-sdk-common")
                description.set("LaunchDarkly SDK Java Common Classes")
                url.set("https://github.com/launchdarkly/java-sdk-common")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        name.set("LaunchDarkly")
                        email.set("team@launchdarkly.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/launchdarkly/java-sdk-common.git")
                    developerConnection.set("scm:git:ssh:git@github.com:launchdarkly/java-sdk-common.git")
                    url.set("https://github.com/launchdarkly/java-sdk-common")
                }
            }
        }
    }
    repositories {
        mavenLocal()
    }
}

nexusPublishing {
    clientTimeout.set(Duration.ofMinutes(2)) // we've seen extremely long delays in creating repositories
    repositories {
        sonatype {
            username.set(ossrhUsername)
            password.set(ossrhPassword)
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
