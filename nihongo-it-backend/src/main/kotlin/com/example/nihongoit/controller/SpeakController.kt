package com.example.nihongoit.controller

import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@EnableScheduling
class SpeakController() {

    @GetMapping("/admin/kaiwa") // Chỉ admin truy cập
    fun getKaiwaForAdmin(): String {
        // Logic tạo hội thoại
        return "Admin kaiwa content"
    }

    @GetMapping("/kaiwa") // User và admin đều truy cập
    fun getKaiwaForUser(): String {
        // Logic tạo hội thoại
        return "User kaiwa content"
    }
}
