plugins {
    id("java")
    id("maven-publish")
}

group = "com.github.softwaresale"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    // logging
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("org.slf4j:slf4j-simple:2.0.7")

    // serialization
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")

    // used to locate our annotation processor
    implementation("com.google.auto.service:auto-service:1.1.1")
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")

    // spring web MVC annotations (TODO this mega bloats my artifact)
    implementation("org.springframework:spring-webmvc:6.1.14")

    // tests
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }
    }
}
