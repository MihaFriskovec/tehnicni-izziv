package com.medifit.scheduling

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@ActiveProfiles("integration")
class SchedulingApplicationTests {

    @Test
    fun contextLoads() {
    }

}
