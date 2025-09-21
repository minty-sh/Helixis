plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(libs.guava)
    implementation(libs.picocli)
    annotationProcessor(libs.picocli.codegen)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass.set("sh.minty.helixis.App")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Aproject=sh.minty/helixis")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
