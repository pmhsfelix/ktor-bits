package first

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.request.ApplicationRequest
import io.ktor.request.httpMethod
import io.ktor.request.uri
import io.ktor.response.ApplicationResponse
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Application.module() {
    install(DefaultHeaders)
    install(Routing) {
        get("/") {
            val request : ApplicationRequest = call.request
            val method = request.httpMethod
            val uri = request.uri // will not be the absolute URI
            val response : ApplicationResponse = call.response
            response.headers.append("My-Header", "My-Value")
            call.respondText("Request with method=${method.value}, URI=$uri", ContentType.Text.Plain)
        }
    }
}
