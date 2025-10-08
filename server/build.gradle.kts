import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Gradle build configuration for Kotlin Language Server
 * 
 * This build script configures dependencies, compilation settings,
 * and packaging for the Kotlin LSP implementation.
 */

plugins {
    // Kotlin JVM plugin for compiling Kotlin source files
    kotlin("jvm") version "1.9.20"
    
    // Application plugin for creating executable distributions
    application
    
    // Shadow plugin for creating fat JARs with all dependencies
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.kotlinlspp"
version = "1.0.0"

repositories {
    // Use Maven Central for resolving dependencies
    mavenCentral()
}

dependencies {
    // Kotlin standard library
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    
    // LSP4J - Language Server Protocol for Java
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.21.1")
    
    // JSON processing
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    
    // Kotlin compiler for advanced features (optional)
    // implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.20")
    
    // Testing dependencies
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.mockito:mockito-core:5.7.0")
}

/**
 * Configure Java compatibility
 */
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

/**
 * Configure Kotlin compilation
 */
tasks.withType<KotlinCompile> {
    kotlinOptions {
        // Set JVM target to Java 11
        jvmTarget = "11"
        
        // Enable all warnings
        allWarningsAsErrors = false
        
        // Generate Java 8 compatible bytecode features
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

/**
 * Configure test execution
 */
tasks.test {
    useJUnitPlatform()
    
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
    }
}

/**
 * Application configuration
 */
application {
    // Main class for the language server
    mainClass.set("com.kotlinlspp.KotlinLanguageServerKt")
    
    // Application name
    applicationName = "kotlin-language-server"
}

/**
 * Shadow JAR configuration for fat JAR creation
 */
tasks.shadowJar {
    // Set the output JAR file name
    archiveBaseName.set("kotlin-language-server")
    archiveClassifier.set("all")
    archiveVersion.set(project.version.toString())
    
    // Set main class manifest attribute
    manifest {
        attributes["Main-Class"] = "com.kotlinlspp.KotlinLanguageServerKt"
        attributes["Implementation-Title"] = "Kotlin Language Server"
        attributes["Implementation-Version"] = project.version
    }
    
    // Merge service files to avoid conflicts
    mergeServiceFiles()
}

/**
 * Custom task to run the language server in stdio mode
 */
tasks.register("runServer") {
    group = "application"
    description = "Run the Kotlin Language Server in stdio mode"
    
    dependsOn(tasks.shadowJar)
    
    doLast {
        exec {
            commandLine(
                "java",
                "-jar",
                "${project.buildDir}/libs/kotlin-language-server-all-${project.version}.jar"
            )
        }
    }
}

/**
 * Clean task configuration
 */
tasks.clean {
    delete("out", ".gradle", "build")
}
