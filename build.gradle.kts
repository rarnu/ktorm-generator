plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "com.rarnu.ktorm.gen"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.isyscore:common-swing:2.0.1.0")
    // Oracle & Dameng
    implementation(fileTree("lib") {
        include("*.jar")
    })
    // mysql
    implementation("mysql:mysql-connector-java:8.0.33")
    // sql server
    implementation("com.microsoft.sqlserver:mssql-jdbc:9.2.0.jre8")
    // postgresql
    implementation("org.postgresql:postgresql:42.2.27")
    // sqlite
    implementation("org.xerial:sqlite-jdbc:3.34.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}