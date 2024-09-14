package com.example

import com.example.model.Task
import com.example.plugins.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import kotlin.test.*

import com.example.model.Priority
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }


    @Test
    fun testRoot2() = testApplication {
        application {
            module()
        }
        val response = client.get("/")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello World!", response.bodyAsText())
    }

    @Test
    fun testNewEndpoint() = testApplication {
        application {
            module()
        }

        val response = client.get("/test1")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("html", response.contentType()?.contentSubtype)
        assertContains(response.bodyAsText(), "Hello From Ktor")
    }


    @Test
    fun tasksCanBeFoundByPriority() = testApplication {
        application {
            module()
        }

        val response = client.get("/tasks/byPriority/Medium")
        val body = response.bodyAsText()

        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(body, "Mow the lawn")
        assertContains(body, "Paint the fence")
    }

    @Test
    fun invalidPriorityProduces400() = testApplication {
        application {
            module()
        }

        val response = client.get("/tasks/byPriority/Invalid")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun unusedPriorityProduces404() = testApplication {
        application {
            module()
        }

        val response = client.get("/tasks/byPriority/Vital")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

//    @Test
//    fun accordingToPriorityProduces200() = testApplication {
//        application {
//            module()
//        }
//        val priorities = listOf("High", "Medium", "Low")  // List of priority values
//
//        priorities.forEach { priority ->
//            val response = client.get("/tasks/byPriority/$priority")
//            assertEquals(HttpStatusCode.OK, response.status)
//        }
//    }

    @Test
    fun test1() = testApplication {
        application {
            module()
        }
        var priorities = listOf("High","Medium","Low")
        priorities.forEach{priority: String ->
            val res = client.get("/tasks/byPriority/$priority")
            assertEquals(HttpStatusCode.OK, res.status)
        }
    }

    @Test
    fun newTasksCanBeAdded_() = testApplication {
        application {
            module()
        }

        val response1 = client.post("/tasks") {
            header(
                HttpHeaders.ContentType,
                ContentType.Application.FormUrlEncoded.toString()
            )
            setBody(
                listOf(
                    "name" to "swimming",
                    "description" to "Go to the beach",
                    "priority" to "Low"
                ).formUrlEncode()
            )
        }

        assertEquals(HttpStatusCode.NoContent, response1.status)

        val response2 = client.get("/tasks")
        assertEquals(HttpStatusCode.OK, response2.status)
        val body = response2.bodyAsText()

        assertContains(body, "swimming")
        assertContains(body, "Go to the beach")
    }

    @Test
    fun invalidPriorityProduces400_() = testApplication {
        val response = client.get("/tasks/byPriority/Invalid")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun unusedPriorityProduces404_() = testApplication {
        val response = client.get("/tasks/byPriority/Vital")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

//    @Test
//    fun newTasksCanBeAdded() = testApplication {
//        val client = createClient {
//            this@testApplication.install(ContentNegotiation) {
//                json()
//            }
//        }
//
//        val task = Task("swimming", "Go to the beach", Priority.Low)
//        val response1 = client.post("/tasks") {
//            header(
//                HttpHeaders.ContentType,
//                ContentType.Application.Json
//            )
//
//            setBody(task)
//        }
//        assertEquals(HttpStatusCode.NoContent, response1.status)
//
//        val response2 = client.get("/tasks")
//        assertEquals(HttpStatusCode.OK, response2.status)
//
//        val taskNames = response2
//            .body<List<Task>>()
//            .map { it.name }
//
//        assertContains(taskNames, "swimming")
//    }


}
