group = "com.github.senocak"
version = "0.1"

plugins {
    alias(libs.plugins.jvm)

    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(dependencyNotation = "org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation(dependencyNotation = libs.junit.jupiter.engine)
    testRuntimeOnly(dependencyNotation = "org.junit.platform:junit-platform-launcher")
    api(dependencyNotation = libs.commons.math3)
    implementation(dependencyNotation = libs.guava)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.named<Test>(name = "test") {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.senocak"
            artifactId = "regexb"
            version = "0.1"

            from(components["java"])
        }
    }
}
