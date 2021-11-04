package com.example.repos

import com.example.model.Customer
import com.example.model.CustomerCreateRequest

interface CustomerRepo {

    fun save(customerDto: CustomerCreateRequest): String

    fun get(id: String): Customer?

    fun all(): Collection<Customer>

    fun delete(id: String): Boolean

}