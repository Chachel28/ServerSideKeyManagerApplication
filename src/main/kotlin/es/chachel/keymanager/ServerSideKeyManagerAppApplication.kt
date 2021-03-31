package es.chachel.keymanager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class ServerSideKeyManagerAppApplication

fun main(args: Array<String>) {
    runApplication<ServerSideKeyManagerAppApplication>(*args)
}
