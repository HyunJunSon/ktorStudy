//package com.example.plugins
//
//import com.example.model.*
//import io.ktor.http.*
//import io.ktor.server.application.*
//import io.ktor.server.http.content.*
//import io.ktor.server.response.*
//import io.ktor.server.routing.*
//import io.ktor.server.routing.get
//import io.ktor.server.plugins.statuspages.*
//import model.*
//import io.ktor.server.http.content.staticResources
//import io.ktor.server.request.*


//fun Application.configureRouting() {
//
////    install(StatusPages) {
////        exception<IllegalStateException> { call, cause ->
////            call.respondText("App in illegal state as ${cause.message}"),
////        }
////    }
//
//    install(StatusPages) {
//        exception<IllegalStateException> { call, cause ->
//            call.respondText(
//                "App in illegal state as ${cause.message}",
//                status = HttpStatusCode.InternalServerError  // 500 상태 코드 반환
//            )
//        }
//    }
//
//    routing {
//        staticResources("/content", "mycontent")
//
//        get("/") {
//            call.respondText("Hello World!")
//        }
//        get("/test1") {
//            val text = "<h1>Hello From Ktor</h1>"
//            val type = ContentType.parse("text/html")
//            call.respondText(text, type)
//        }
//
//        get("/error-test") {
//            throw IllegalStateException("Too Busy")
//        }
//
//        get("/tasks") {
//            call.respondText(
//                contentType = ContentType.parse("text/html"),
//                text = """
//                <h3>TODO:</h3>
//                <ol>
//                    <li>A table of all the tasks</li>
//                    <li>A form to submit new tasks</li>
//                </ol>
//                """.trimIndent()
//            )
//        }
//    }
//}

//fun Application.configureRouting() {
//    routing {
//        get("/tasks") {
//            call.respondText(
//                contentType = ContentType.parse("text/html"),
//                text = tasks.tasksAsTable()
//            )
//        }
//    }
//}


//fun Application.configureRouting() {
//    routing {
//
//        staticResources("/task-ui", "task-ui")
//
//        route("/tasks"){
//            get (){
//                val tasks = TaskRepository.allTasks()
//                call.respondText(
//                    contentType = ContentType.parse("text/html"),
//                    text = tasks.tasksAsTable()
//                )
//            }
//
//            get("/byPriority/{priority}") {
//                val priorityAsText = call.parameters["priority"]
//                if (priorityAsText == null) {
//                    call.respond(HttpStatusCode.BadRequest)
//                    return@get
//                }
//
//                try {
//                    val priority = Priority.valueOf(priorityAsText)
//                    val tasks = TaskRepository.tasksByPriority(priority)
//
//                    if (tasks.isEmpty()) {
//                        call.respond(HttpStatusCode.NotFound)
//                        return@get
//                    }
//
//                    call.respondText(
//                        contentType = ContentType.parse("text/html"),
//                        text = tasks.tasksAsTable()
//                    )
//                } catch(ex: IllegalArgumentException) {
//                    call.respond(HttpStatusCode.BadRequest)
//                }
//            }
//
//
//            //add the following route
//            post() {
//                val formContent = call.receiveParameters()
//
//                val params = Triple(
//                    formContent["name"] ?: "",
//                    formContent["description"] ?: "",
//                    formContent["priority"] ?: ""
//                )
//
//                if (params.toList().any { it.isEmpty() }) {
//                    call.respond(HttpStatusCode.BadRequest)
//                    return@post
//                }
//
//                try {
//                    val priority = Priority.valueOf(params.third)
//                    TaskRepository.addTask(
//                        Task(
//                            params.first,
//                            params.second,
//                            priority
//                        )
//                    )
//                    call.respond(HttpStatusCode.NoContent)
//                } catch (ex: IllegalArgumentException) {
//                    call.respond(HttpStatusCode.BadRequest)
//                } catch (ex: IllegalStateException) {
//                    call.respond(HttpStatusCode.BadRequest)
//                }
//            }
//        }
//    }
//}

//package com.example.plugins
//
//import com.example.model.*
//import io.ktor.server.application.*
//import io.ktor.server.http.content.*
//import io.ktor.server.response.*
//import io.ktor.server.routing.*
//
//fun Application.configureRouting() {
//    routing {
//        staticResources("static", "static")
//
//        get("/tasks") {
//            call.respond(
//                listOf(
//                    Task("cleaning", "Clean the house", Priority.Low),
//                    Task("gardening", "Mow the lawn", Priority.Medium),
//                    Task("shopping", "Buy the groceries", Priority.High),
//                    Task("painting", "Paint the fence", Priority.Medium)
//                )
//            )
//        }
//    }
//}


package example.com.plugins

import com.example.model.Priority
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.TaskRepository
import com.example.model.Task
import io.ktor.serialization.*
import io.ktor.server.request.*

fun Application.configureRouting() {
    routing {
        staticResources("static", "static")

        //updated implementation
        route("/tasks") {
            get {
                val tasks = TaskRepository.allTasks()
                call.respond(tasks)
            }

            get("/byName/{taskName}") {
                val name = call.parameters["taskName"]
                if (name == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val task = TaskRepository.taskByName(name)
                if (task == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                call.respond(task)
            }
            get("/byPriority/{priority}") {
                val priorityAsText = call.parameters["priority"]
                if (priorityAsText == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val priority = Priority.valueOf(priorityAsText)
                    val tasks = TaskRepository.tasksByPriority(priority)

                    if (tasks.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(tasks)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
            post {
                try {
                    val task = call.receive<Task>()
                    TaskRepository.addTask(task)
                    call.respond(HttpStatusCode.NoContent)
                } catch (ex: IllegalStateException) {
                    call.respond(HttpStatusCode.BadRequest)
                } catch (ex: JsonConvertException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            delete("/{taskName}") {
                val name = call.parameters["taskName"]
                if (name == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@delete
                }
                if (TaskRepository.removeTask(name)) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }

    }
}