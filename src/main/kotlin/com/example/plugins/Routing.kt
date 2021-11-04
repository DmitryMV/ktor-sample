package com.example.plugins

import io.ktor.application.*
import io.ktor.routing.*

fun Application.configureRouting() {
    routing {
        customerRouting()
    }
}
