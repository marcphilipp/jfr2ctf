plugins {
    application
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(15))
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:16.0.2")

    val immutablesVersion = "2.8.2"
    annotationProcessor("org.immutables:value:${immutablesVersion}")
    compileOnly("org.immutables:value:${immutablesVersion}:annotations")
    compileOnly("javax.annotation:javax.annotation-api:1.3.2") {
        because("otherwise the generated classes are shown as having compile errors in IDEA")
    }

    implementation("info.picocli:picocli:4.5.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.3")
    implementation("org.apache.commons:commons-lang3:3.11")
    implementation("commons-io:commons-io:2.8.0")
}

application {
    mainClass.set("de.marcphilipp.jfr2ctf.Cli")
    applicationDefaultJvmArgs = listOf("--enable-preview")
}

tasks {
    compileJava {
        options.apply {
            compilerArgs.add("--enable-preview")
            compilerArgs.add("-Aimmutables.gradle.incremental")
            release.set(15)
        }
    }
    test {
        useJUnitPlatform()
        jvmArgs("--enable-preview")
    }
}
