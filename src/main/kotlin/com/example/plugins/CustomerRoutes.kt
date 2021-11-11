package com.example.plugins

import com.example.model.CustomerCreateRequest
import com.example.repos.CustomerRepo
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.Accepted
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject

fun Route.customerRouting() {
    val customerRepo by inject<CustomerRepo>()
    route("/customer") {
        get {
            call.respond(ArrayList(customerRepo.all()))
        }
        get("{id}") {
            val id = call.parameters["id"] as String
            val customer = customerRepo.get(id)
            if (customer != null) {
                call.respond(customer)
            } else {
                call.respondText(
                    "customer not found",
                    status = NotFound
                )
            }
        }
        post {
            val customerDto = call.receive<CustomerCreateRequest>()
            val id = customerRepo.save(customerDto)
            call.respond(HttpStatusCode.Created, id)
        }
        delete("{id}") {
            val id = call.parameters["id"]
            if (id == null) {
                call.respondText("id required", status = BadRequest)
            } else {
                if (customerRepo.delete(id)) {
                    call.respondText("removed customer with id=$id", status = Accepted)
                } else {
                    call.respondText("customer with id=$id not found", status = NotFound)
                }
            }
        }
    }
}