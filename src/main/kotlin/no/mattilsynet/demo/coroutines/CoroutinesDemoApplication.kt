package no.mattilsynet.demo.coroutines

import kotlin.system.measureTimeMillis

class CoroutinesDemoApplication() : Runnable {
    override fun run() {
        val demo = Demo()
        println("Starter demo")

        val elapsed = measureTimeMillis {
//            demo.demo1()
//            demo.demo2()
//            demo.demo3()
            demo.demo4()
        }

        println("Demo ferdig etter ${elapsed} millis")
    }
}


fun main() {
    CoroutinesDemoApplication().run()
}
