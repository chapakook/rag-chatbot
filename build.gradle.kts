plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.chapakook.lab"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // web
    implementation("org.springframework.boot:spring-boot-starter-web") // 기본 REST API (Servlet)
    implementation("org.springframework.boot:spring-boot-starter-webflux") // WebClient (비동기 HTTP)

    // json
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin") // Kotlin <-> JSON 직렬화

    // kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect") // Kotlin 리플렉션 (DI 등)
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions") // Reactor + Kotlin DSL 지원
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor") // Coroutine ↔ Reactor 변환

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test") // Spring 기본 테스트
    testImplementation("io.projectreactor:reactor-test") // Mono/Flux 테스트 도구
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5") // Kotlin DSL + JUnit5 연동
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test") // Coroutine 테스트 도구
    testRuntimeOnly("org.junit.platform:junit-platform-launcher") // JUnit 런처 (IDE 실행용)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
