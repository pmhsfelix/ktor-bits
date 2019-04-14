package first

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.auth.basic.BasicAuth
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.get
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.response.ApplicationResponse
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("module")

fun Application.module() {
    install(DefaultHeaders)
    install(ExampleFeature)
    val client = HttpClient(Apache){
        install(BasicAuth) {
            username = "alice"
            password = "password"
        }
        install(Logging) {
            level = LogLevel.HEADERS
        }
    }
    install(Routing) {
        get("/") {
            logger.info("route '/")
            val response : ApplicationResponse = call.response
            response.headers.append("My-Header", "My-Value")
            val echoString = client.get<String>("https://httpbin.org/get")
            call.respondText(echoString, ContentType.Text.Plain)
        }
    }
}
