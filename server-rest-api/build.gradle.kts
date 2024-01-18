plugins {
    id("java")
    id("io.spring.dependency-management") version "1.1.4"
    id("org.springdoc.openapi-gradle-plugin") version "1.8.0"
    id("com.bmuschko.docker-remote-api") version "9.4.0"
    id("io.swagger.core.v3.swagger-gradle-plugin") version "2.2.20"
}


apply(from = "../build/javaproject.gradle")
evaluationDependsOn(":api-openbis-java")
evaluationDependsOn(":server-application-server")


group = "ch.empa"
version = "SNAPSHOT-r1705419459"

repositories {
    mavenCentral()
    maven {
        url = uri("https://plugins.gradle.org/m2/")

    }

}

sourceSets {
    main {
        java {
            srcDirs("source/main/java")
        }
        resources {
            srcDirs("source/main/resources")
        }
    }
    test {
        java {
            srcDirs("source/test/java")
        }
        resources {
            srcDirs("source/test/resources")
        }
    }
}


val springVersion = "5.3.31"
val springBootVersion = "2.7.14"


dependencyManagement {

    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${springBootVersion}") {
            bomProperty("spring-framework.version", springVersion)
        }
        mavenBom("org.springframework.boot:spring-boot-starter-security:${springBootVersion}") {
            bomProperty("spring-framework.version", springVersion)
        }
        mavenBom("org.springframework.boot:spring-boot-starter-web:${springBootVersion}") {
            bomProperty("spring-framework.version", springVersion)
        }

        mavenBom("org.springframework.boot:spring-boot-starter-validation:${springBootVersion}") {
            bomProperty("spring-framework.version", springVersion)
        }

        mavenBom("org.springframework:spring-framework-bom:${springVersion}") {
            bomProperty("spring-framework.version", springVersion)
        }
    }
}

tasks.bootRun{
    dependsOn(":server-application-server:openBISDevelopmentEnvironmentASStart")
}
springBoot {
    mainClass.set("ch.empa.openbisrest.OpenbisRestServer")


}


dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.springframework.boot:spring-boot-starter-validation:${springBootVersion}")
    implementation("org.springframework.boot:spring-boot-starter-web:${springBootVersion}")
    implementation("org.springframework.boot:spring-boot-starter-tomcat:${springBootVersion}")
    implementation("org.springdoc:springdoc-openapi-ui:1.7.0")
    implementation("org.springdoc:springdoc-openapi-webmvc-core:1.7.0")
    implementation("org.hibernate.validator:hibernate-validator:6.2.5.Final")
    implementation("org.springframework.boot:spring-boot-starter-security:${springBootVersion}")
    implementation("org.springframework.boot:spring-boot-starter-graphql:${springBootVersion}")
    implementation(project(":api-openbis-java"))
    compileOnly(project(":server-application-server")){
        exclude(group =  "ch.qos.logback", module="logback-classic")
    }


}

configurations {
    all {
        exclude(group =  "ch.qos.logback", module="logback-classic")
    }
}



tasks.test {
    useJUnitPlatform()
}