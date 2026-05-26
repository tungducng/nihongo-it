package io.github.ndtung723.nihongoit.userservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["io.github.ndtung723.nihongoit.userservice", "io.github.ndtung723.nihongoit.common"])
@EnableDiscoveryClient
@EnableFeignClients(basePackages = ["io.github.ndtung723.nihongoit.userservice.feign"])
@EnableScheduling
class UserServiceApplication

fun main(args: Array<String>) {
    runApplication<UserServiceApplication>(*args)
}
