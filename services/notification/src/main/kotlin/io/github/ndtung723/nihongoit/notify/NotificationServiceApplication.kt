package io.github.ndtung723.nihongoit.notify

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["io.github.ndtung723.nihongoit.notify", "io.github.ndtung723.nihongoit.common"])
@EnableDiscoveryClient
@EnableScheduling
class NotificationServiceApplication

fun main(args: Array<String>) {
    runApplication<NotificationServiceApplication>(*args)
}
