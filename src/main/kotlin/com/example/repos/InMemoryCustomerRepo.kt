package com.example.repos

import com.example.model.Customer
import com.example.model.CustomerCreateRequest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class InMemoryCustomerRepo : CustomerRepo {

    private val idCounter = AtomicInteger(0)
    private val customers: MutableMap<String, Customer> = ConcurrentHashMap()

    override fun save(customerDto: CustomerCreateRequest): String {
        val id = "${idCounter.incrementAndGet()}"
        val customer = Customer(
            id,
            customerDto.firstName,
            customerDto.lastName,
            customerDto.email
        )
        customers[id] = customer
        return id
    }

    override fun get(id: String): Customer? {
        return customers[id]
    }

    override fun all(): Collection<Customer> {
        return customers.values;
    }

    override fun delete(id: String): Boolean {
        return customers.remove(id) != null
    }
}