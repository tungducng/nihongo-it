plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "1.9.25"

    id("org.jlleitschuh.gradle.ktlint") version "12.2.0" // Thêm plugin ktlint

    // OpenAPI
    id("org.openapi.generator") version "6.6.0"

    // jacoco
    id("jacoco")
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

extra["springAiVersion"] = "1.0.0-M6"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Add Spring Boot DevTools
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("org.postgresql:postgresql")
    
    // Spring Boot test with Mockito excluded
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.mockito")
    }
    
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5") // Dùng Jackson để parse JSON

    // doc-openapi
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
    implementation("org.springdoc:springdoc-openapi-starter-common:2.7.0")

    // mock - explicit versions
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.0")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")

    // Add Kuromoji for Japanese text analysis
    implementation("com.atilika.kuromoji:kuromoji-ipadic:0.9.0")

    // Add OAuth2 client support
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    
    // Add Email support
    implementation("org.springframework.boot:spring-boot-starter-mail")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Cấu hình JaCoCo
jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // Chạy test trước khi tạo báo cáo
    reports {
        xml.required.set(true) // Báo cáo XML (dùng cho CI/CD)
        csv.required.set(false) // Báo cáo CSV (tuỳ chọn)
        html.required.set(true) // Báo cáo HTML (dễ đọc)
    }
}

openApiGenerate {
    generatorName.set("typescript-axios") // Generator cho TypeScript client
    inputSpec.set("$projectDir/openapi.json")
    outputDir.set("$projectDir/../nihongo-it-frontend/backend") // Lưu vào thư mục frontend/backend
    apiPackage.set("api") // Package cho API client
    modelPackage.set("model") // Package cho models/DTO
    configOptions.set(
        mapOf(
            "useSingleRequestParameter" to "true",
            "withSeparateModelsAndApi" to "true",
            "modelPropertyNaming" to "camelCase",
            "supportsES6" to "true",
        ),
    )
}

ktlint {
    version.set("0.50.0") // Phiên bản ktlint
    verbose.set(true) // Hiển thị chi tiết khi kiểm tra
    outputToConsole.set(true) // In kết quả ra console
    outputColorName.set("RED") // Màu sắc cho lỗi
    filter {
        exclude("**/generated/**") // Loại trừ thư mục generated
        include("**/kotlin/**") // Chỉ kiểm tra file Kotlin
    }
}
