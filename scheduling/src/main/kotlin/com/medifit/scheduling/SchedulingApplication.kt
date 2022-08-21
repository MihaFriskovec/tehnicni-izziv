package com.medifit.scheduling

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.ComponentScan


@EnableFeignClients
@SpringBootApplication
class SchedulingApplication

fun main(args: Array<String>) {
    runApplication<SchedulingApplication>(*args)
}
