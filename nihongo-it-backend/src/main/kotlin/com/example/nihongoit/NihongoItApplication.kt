package com.example.nihongoit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling  // Enable scheduling of tasks
@EnableFeignClients // Enable Feign clients for microservice communication
class NihongoItApplication

fun main(args: Array<String>) {
    runApplication<NihongoItApplication>(*args)
}