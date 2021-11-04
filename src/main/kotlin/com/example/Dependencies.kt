package com.example

import com.example.repos.CustomerRepo
import com.example.repos.InMemoryCustomerRepo
import io.ktor.application.*
import org.koin.dsl.module
import org.koin.ktor.ext.Koin

val mainModule = module {
    single { InMemoryCustomerRepo() as CustomerRepo }
}

fun Application.configureKoinDeps(){
    install(Koin){
        modules(mainModule);
    }
}