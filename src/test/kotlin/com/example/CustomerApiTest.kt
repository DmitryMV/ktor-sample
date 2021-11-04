package com.example

import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.utils.io.charsets.*
import net.javacrumbs.jsonunit.assertj.assertThatJson
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class CustomerApiTest {
    @Test
    fun `empty customer list returned`() {
        test {
            handleRequest(HttpMethod.Get, "/customer").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.contentType()).isEqualTo(
                    ContentType.Application.Json.withCharset(Charset.forName("UTF-8"))
                )
                assertThat(response.content).isNotNull
                assertThatJson(response.content!!).isEqualTo("[]")
            }
        }
    }

    @Test
    fun `create new customer`() {
        test {
            handleRequest(HttpMethod.Post, "/customer") {
                addHeader("Content-Type", ContentType.Application.Json.toString())
                setBody(
                    """
                {
                    "firstName": "FirstName1",
                    "lastName": "LastName",
                    "email": "email@domain.com"
                }
                """.trimIndent()
                )
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Created)
                val id = response.content
                assertThat(id).isNotNull.containsOnlyDigits()
            }
        }
    }

    @Test
    fun `get just created customer by id`() {
        test {
            val id = handleRequest(HttpMethod.Post, "/customer") {
                addHeader("Content-Type", ContentType.Application.Json.toString())
                setBody(
                    """
                {
                    "firstName": "FirstName1",
                    "lastName": "LastName1",
                    "email": "email@domain.com"
                }
                """.trimIndent()
                )
            }.let {
                assertThat(it.response.status()).isEqualTo(HttpStatusCode.Created)
                val id = it.response.content
                assertThat(id).isNotNull.containsOnlyDigits()
                id
            }
            handleRequest(HttpMethod.Get, "/customer/$id").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.contentType()).isEqualTo(
                    ContentType.Application.Json.withCharset(Charset.forName("UTF-8"))
                )
                val responseJson = response.content
                assertThat(responseJson).isNotNull
                assertThatJson(responseJson!!).isEqualTo(
                    """{
                        "id":"1",
                        "lastName": "LastName1",
                        "firstName": "FirstName1",
                        "email": "email@domain.com"
                    }
                """.trimIndent()
                )
            }
        }
    }

    @Test
    fun `get non existing customer by id`() {
        test {
            handleRequest(HttpMethod.Get, "/customer/1").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }
        }
    }

    @Test
    fun `delete non existing customer by id`() {
        test {
            handleRequest(HttpMethod.Delete, "/customer/1").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }
        }
    }

    @Test
    fun `get just created customer in a list`() {
        test {
            handleRequest(HttpMethod.Post, "/customer") {
                addHeader("Content-Type", ContentType.Application.Json.toString())
                setBody(
                    """
                {
                    "firstName": "FirstName1",
                    "lastName": "LastName1",
                    "email": "email@domain.com"
                }
                """.trimIndent()
                )
            }.let {
                assertThat(it.response.status()).isEqualTo(HttpStatusCode.Created)
                val id = it.response.content
                assertThat(id).isNotNull.containsOnlyDigits()
                id
            }
            handleRequest(HttpMethod.Get, "/customer").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.contentType()).isEqualTo(
                    ContentType.Application.Json.withCharset(Charset.forName("UTF-8"))
                )
                val responseJson = response.content
                assertThat(responseJson).isNotNull
                assertThatJson(responseJson!!).isEqualTo(
                    """[
                        {
                            "id":"1",
                            "lastName": "LastName1",
                            "firstName": "FirstName1",
                            "email": "email@domain.com"
                        }
                    ]
                """.trimIndent()
                )
            }
        }
    }

    @Test
    fun `delete just created customer`() {
        test {
            val id = handleRequest(HttpMethod.Post, "/customer") {
                addHeader("Content-Type", ContentType.Application.Json.toString())
                setBody(
                    """
                {
                    "firstName": "FirstName1",
                    "lastName": "LastName1",
                    "email": "email@domain.com"
                }
                """.trimIndent()
                )
            }.let {
                assertThat(it.response.status()).isEqualTo(HttpStatusCode.Created)
                val id = it.response.content
                assertThat(id).isNotNull.containsOnlyDigits()
                id
            }
            handleRequest(HttpMethod.Delete, "/customer/$id").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Accepted)
            }
            handleRequest(HttpMethod.Get, "/customer").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.contentType()).isEqualTo(
                    ContentType.Application.Json.withCharset(Charset.forName("UTF-8"))
                )
                val responseJson = response.content
                assertThat(responseJson).isNotNull
                assertThatJson(responseJson!!).isEqualTo("[]")
            }
        }
    }

    private fun <R> test(test: TestApplicationEngine.() -> R) {
        withTestApplication({
            configureKoinDeps()
            configureSerialization()
            configureRouting()
        }, test)
    }
}