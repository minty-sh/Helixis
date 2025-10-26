plugins {
    application
    id("com.diffplug.spotless") version "8.0.0"
}

spotless {
    java {
        eclipse().configFile(rootProject.file("config/spotless/eclipse.xml"))

        // Enforce 4-space indentation (decent enough fallback)
        leadingTabsToSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
        target("src/**/*.java")

        // Fix imports
        importOrder()
        removeUnusedImports()
    }
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
