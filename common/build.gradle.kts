plugins {
    id("java-library")
}

dependencies {
    api("org.slf4j:slf4j-api:1.7.21")
    testImplementation("junit:junit:4.13.1")
    testImplementation("org.slf4j:slf4j-jdk14:1.7.21")
    testImplementation("org.apache.commons:commons-io:1.3.2")
}

description = "robostroke-common"
