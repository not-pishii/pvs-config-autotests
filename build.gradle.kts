plugins {
    java
    application
}

group = "com.pvsstudio.practice"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    implementation(platform("org.junit:junit-bom:6.1.1"))
    implementation("org.junit.jupiter:junit-jupiter")
    implementation("org.junit.platform:junit-platform-launcher")
    implementation("org.assertj:assertj-core:3.27.7")
}

application {
    mainClass = "com.pvsstudio.practice.analyzer.CliAnalyzerAutotests"
}
