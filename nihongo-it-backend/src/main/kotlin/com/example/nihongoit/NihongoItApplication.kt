package com.example.nihongoit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling  // Enable scheduling of tasks
class NihongoItApplication

fun main(args: Array<String>) {
    runApplication<NihongoItApplication>(*args)
} 