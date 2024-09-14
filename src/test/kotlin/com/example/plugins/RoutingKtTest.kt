package com.example.plugins

import com.example.module
import io.ktor.client.request.*
import io.ktor.server.testing.*
import kotlin.test.Test

class RoutingKtTest {

    @Test
    fun testGetErrortest() = testApplication {
        application {
            module()
        }
        client.get("/error-test").apply {
            TODO("Please write your test here")
        }
    }
}