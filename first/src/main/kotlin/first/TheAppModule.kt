package first

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.auth.basic.BasicAuth
import io.ktor.client.request.get
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.response.ApplicationResponse
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("module")

fun Application.module() {
    install(DefaultHeaders)
    val client = HttpClient(Apache) {
        install(BasicAuth) {
            username = "alice"
            password = "password"
        }
        install(ExampleHttpClientFeature) {
            headerName = "My-Header"
            headerValue = "My-Value"
        }
    }
    install(Routing) {
        get("/foo") {
            // This block during request-time (i.e. for each request)
            logger.info("route '/foo")
            val response: ApplicationResponse = call.response
            response.headers.append("My-Header", "My-Value")
            val echoString = client.get<String>("https://httpbin.org/get")
            call.respondText(echoString, ContentType.Text.Plain)
        }
        route("/bar", HttpMethod.Get) {
            // This block is executed during build-time
            // This feature will only run for this route
            install(ExampleApplicationCallFeature)
            handle {
                // This block during request-time (i.e. for each request)
                logger.info("route '/bar")
                val response: ApplicationResponse = call.response
                response.headers.append("My-Header", "My-Value")
                val echoString = client.get<String>("https://httpbin.org/get")
                call.respondText(echoString, ContentType.Text.Plain)
            }
        }
    }
}
