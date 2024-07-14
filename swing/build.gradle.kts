plugins {
    java
    application
}

dependencies {
    implementation(project(":common"))
    implementation("org.slf4j:slf4j-jdk14:1.7.21")
    implementation("com.google.zxing:javase:2.2")
    implementation("net.java.dev.jna:jna:3.5.2")
    implementation("uk.co.caprica:vlcj:3.7.0")
    testImplementation("junit:junit:4.13.1")
}

application {
    mainClass = "org.nargila.robostroke.app.RoboStrokeSwing"
}

description = "robostroke-swing"
