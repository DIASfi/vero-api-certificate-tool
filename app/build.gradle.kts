/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.2/userguide/building_java_projects.html
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    // https://github.com/boothen/gradle-wsimport
    id("uk.co.boothen.gradle.wsimport") version "0.18" apply(true)
    id("com.github.johnrengelman.shadow") version "7.0.0" apply(true)
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit test framework.
    testImplementation("junit:junit:4.13.2")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:30.1.1-jre")

    // https://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk15on
    implementation("org.bouncycastle:bcprov-jdk15on:1.69")

    // https://mvnrepository.com/artifact/org.bouncycastle/bcutil-jdk15on
    implementation("org.bouncycastle:bcutil-jdk15on:1.69")

    // https://mvnrepository.com/artifact/org.bouncycastle/bcpkix-jdk15on
    implementation("org.bouncycastle:bcpkix-jdk15on:1.69")

    // https://mvnrepository.com/artifact/com.sun.xml.ws/jaxws-rt
    implementation("com.sun.xml.ws:jaxws-rt:3.0.2")

    // https://mvnrepository.com/artifact/com.sun.org.apache.xml.internal/resolver
    implementation("com.sun.org.apache.xml.internal:resolver:20050927")

    // https://picocli.info/#_add_as_external_dependency
    implementation("info.picocli:picocli:4.6.1")
    annotationProcessor("info.picocli:picocli-codegen:4.6.1")

    // https://github.com/elennick/retry4j#gradle
    implementation("com.evanlennick:retry4j:0.15.0")
}

application {
    // Define the main class for the application.
    mainClass.set("fi.dias.tools.vero.App")
}

tasks {

    shadowJar {
        append("wsdl/CertificateServices.wsdl")
        append("wsdl/CertificateServices.xsd")
        append("wsdl/xmldsig-core-schema.xsd")
        manifest {
            attributes(Pair("Main-Class", application.mainClass))
        }
    }

    wsimport {
        includeDependencies = true
        target = "3.0"
        verbose = true
        quiet = false
        debug = false

        wsdl("CertificateServices.wsdl") {}
    }
}