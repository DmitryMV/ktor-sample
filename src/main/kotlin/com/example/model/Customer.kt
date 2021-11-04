package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class CustomerCreateRequest(
    val firstName: String,
    val lastName: String,
    val email: String
)

@Serializable
data class Customer(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String
)