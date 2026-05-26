package io.github.ndtung723.nihongoit.eurekaserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

@SpringBootApplication
@EnableEurekaServer
class EurekaServerApplication

fun main(args: Array<String>) {
    runApplication<EurekaServerApplication>(*args)
}
