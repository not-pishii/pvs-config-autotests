plugins {
    java
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
    testImplementation(platform("org.junit:junit-bom:6.1.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()

    System.getProperty("pvsJsPath")
        ?.takeIf { it.isNotBlank() }
        ?.let { systemProperty("pvsJsPath", it) }

    System.getenv("PVS_JS_PATH")
        ?.takeIf { it.isNotBlank() }
        ?.let { environment("PVS_JS_PATH", it) }
}
