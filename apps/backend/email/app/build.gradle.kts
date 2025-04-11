import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("com.google.devtools.ksp")
    id("com.github.johnrengelman.shadow")
    id("io.micronaut.application")
    id("io.micronaut.aot")
    id("io.micronaut.test-resources")
    id("org.jlleitschuh.gradle.ktlint")
}

ktlint {
    version.set(project.properties["ktlint.version"] as String)
    android.set(false)
    outputToConsole.set(true)
    outputColorName.set("RED")
    reporters {
        reporter(ReporterType.CHECKSTYLE)
        reporter(ReporterType.HTML)
    }
}
tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask>().configureEach {
    enabled = true
}

tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask>().configureEach {
    enabled = true
}

version = "0.1"
group = "top.sunbath.api.email"

dependencies {
    // Platform BOMs
    implementation(platform(libs.aws.sdk.bom))
    implementation(platform(libs.micronaut.bom))

    // KSP/Annotation Processors
    ksp(libs.micronaut.http.validation)
    ksp(libs.micronaut.serde.processor)

    // Project Dependencies
    implementation(project(":libs:jvm-shared-lib"))

    // Micronaut Dependencies
    implementation(libs.micronaut.cache.caffeine)
    implementation(libs.micronaut.serde.jackson)
    implementation(libs.micronaut.validation)
    implementation(libs.micronaut.aws.sdk.v2)
    runtimeOnly(libs.micronaut.aws.lambda.events.serde)
    runtimeOnly(libs.micronaut.http.client.jdk)

    // AWS SDK Dependencies
    implementation(libs.aws.dynamodb)
    implementation(libs.aws.lambda.java.events)
    implementation(libs.aws.ssm)

    // Other Third-Party Dependencies
    implementation(libs.jackson.databind)
    implementation(libs.jakarta.validation)
    implementation(libs.resend)
    runtimeOnly(libs.jackson.module.kotlin)
    runtimeOnly(libs.logback)
    runtimeOnly(libs.snakeyaml)

    // Test Dependencies
}

application {
    mainClass = "top.sunbath.api.email.ApplicationKt"
}
java {
    sourceCompatibility = JavaVersion.toVersion("21")
}

graalvmNative.toolchainDetection = false

micronaut {
    runtime("lambda_java")
    testRuntime("junit5")
    nativeLambda {
        lambdaRuntimeClassName = "io.micronaut.function.aws.runtime.MicronautLambdaRuntime"
    }
    processing {
        incremental(true)
        annotations("top.sunbath.api.email.*")
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
    }
}

tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
    jdkVersion = "21"
    args(
        "-XX:MaximumHeapSizePercent=80",
        "-Dio.netty.allocator.numDirectArenas=0",
        "-Dio.netty.noPreferDirect=true",
    )
}
