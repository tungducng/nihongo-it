package io.github.ndtung723.nihongoit.learningservice

import io.github.ndtung723.nihongoit.learningservice.config.FsrsProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["io.github.ndtung723.nihongoit.learningservice", "io.github.ndtung723.nihongoit.common"])
@EnableDiscoveryClient
@EnableScheduling
@EnableCaching
@EnableConfigurationProperties(FsrsProperties::class)
class LearningServiceApplication

fun main(args: Array<String>) {
    runApplication<LearningServiceApplication>(*args)
}
