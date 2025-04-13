//package com.example.nihongoit.config
//
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.context.annotation.Profile
//import org.springframework.mail.javamail.JavaMailSender
//import org.springframework.mail.javamail.JavaMailSenderImpl
//import java.util.Properties
//
///**
// * Email configuration for the application
// * Provides JavaMailSender beans for different environments
// */
//@Configuration
//class EmailConfig {
//
//    /**
//     * Development environment email sender
//     * This is a mock implementation that doesn't actually send emails
//     */
//    @Bean
//    @Profile("dev")
//    fun javaMailSenderDev(): JavaMailSender {
//        val mailSender = JavaMailSenderImpl()
//        mailSender.host = "localhost"
//        mailSender.port = 1025 // Typically used by MailHog or similar email testing tools
//        mailSender.username = "no-reply@nihongoit.com"
//        mailSender.password = "password"
//
//        val props = mailSender.javaMailProperties
//        props["mail.transport.protocol"] = "smtp"
//        props["mail.smtp.auth"] = "false" // No auth for dev environment
//        props["mail.smtp.starttls.enable"] = "false"
//        props["mail.debug"] = "true"
//
//        return mailSender
//    }
//
//    /**
//     * Production environment email sender
//     * This uses actual SMTP settings for sending real emails
//     */
//    @Bean
//    @Profile("prod")
//    fun javaMailSenderProd(): JavaMailSender {
//        val mailSender = JavaMailSenderImpl()
//        // These would typically be set via environment properties
//        mailSender.host = "smtp.example.com"
//        mailSender.port = 587
//        mailSender.username = "your-email@example.com"
//        mailSender.password = "your-password"
//
//        val props = mailSender.javaMailProperties
//        props["mail.transport.protocol"] = "smtp"
//        props["mail.smtp.auth"] = "true"
//        props["mail.smtp.starttls.enable"] = "true"
//
//        return mailSender
//    }
//}