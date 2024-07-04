package no.mattilsynet.demo.coroutines

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

class Demo {

    // Standard med suspend trigger (delay)
    fun demo1() = runBlocking {
        repeat(10000) {
            launch {
                println("Start Launch 1 ${Thread.currentThread().name}")
                delay(1000)
                println("Finished Launch 1 ${Thread.currentThread().name}")
            }
            launch {
                launchMe(2000)
            }
        }
//        launchMe(1)
        println("Running...")
    }

    // Dispatchers
    fun demo2() = runBlocking {
        repeat(10000) {
            launch(Dispatchers.Default) {
                delay(1000)
                println("Launch 1 ${Thread.currentThread().name}")
            }
            launch {
                launchMe(2000)
            }
        }
        println("Running...")
    }

    // Scope - runBlocking vs coroutineScope
    fun demo3() = runBlocking {
        repeat(1) {
            launch(Dispatchers.Default) {
                runBlocking {
                    println("Start launchBlocking ${Thread.currentThread().name}")
                    delay(1000)
                    println("Finished launchBlocking ${Thread.currentThread().name}")
                }
            }
            launch(Dispatchers.Default) {
                coroutineScope() {
                    println("Start launchSuspending ${Thread.currentThread().name}")
                    delay(2000)
                    println("Finished launchSuspending ${Thread.currentThread().name}")
                }
            }
        }
        println("Running...")
    }

    fun demo4() = runBlocking {

        // async, composable sync
        val deferred = async { getAvailablePets() }
        val availablePets = deferred.await()

        val job = launch {
            repeat(10000) {
                delay(10)
                println("Doing something on ${Thread.currentThread().name}")
            }
        }

        availablePets.asFlow().map {
            println("Getting pet ${it.id} on thread ${Thread.currentThread().name}")
            getPet(it.id)
            println("Got pet ${it.id} on thread ${Thread.currentThread().name}")
            it
        }.collect()

        job.cancelAndJoin()

    }

    data class Pet(val id: String)

    private suspend fun getAvailablePets(): List<Pet> {
        val client = HttpClient()
        val response: HttpResponse = client.get("https://petstore.swagger.io/v2/pet/findByStatus?status=pending")
        val objectMapper =
            ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).registerModule(
                KotlinModule()
            )
        return objectMapper.readValue(response.bodyAsText(), object : TypeReference<List<Pet>>() {})
    }

    private suspend fun getPet(id: String): Pet? {

        val client = HttpClient()
        val response: HttpResponse = client.get("https://petstore.swagger.io/v2/pet/${id}")
        val objectMapper =
            ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).registerModule(
                KotlinModule()
            )
        return response
            .takeIf { it.status == HttpStatusCode.OK }
            .let { objectMapper.readValue(response.bodyAsText(), Pet::class.java) }
    }

    private suspend fun launchMe(timeMillis: Long) {
        println("Start LaunchMe ${Thread.currentThread().name}")
        delay(timeMillis)
        println("Finished LaunchMe ${Thread.currentThread().name}")
    }
}