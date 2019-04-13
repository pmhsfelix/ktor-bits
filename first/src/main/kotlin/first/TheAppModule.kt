package first

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Application.module() {
    install(DefaultHeaders)
    install(Routing) {
        get("/") {
            call.respondText("Hello World from ktor, using modules", ContentType.Text.Plain)
        }
    }
}
