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

    val recordBuilderVersion = "1.14.ea"
    annotationProcessor("io.soabase.record-builder:record-builder-processor:${recordBuilderVersion}")
    compileOnly("io.soabase.record-builder:record-builder-core:${recordBuilderVersion}")

    implementation("info.picocli:picocli:4.5.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.0")
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
            forkOptions.jvmArgs = listOf("--enable-preview")
            compilerArgs.add("--enable-preview")
            release.set(15)
        }
    }
    test {
        useJUnitPlatform()
        jvmArgs("--enable-preview")
    }
}
