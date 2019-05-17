package first

import io.ktor.client.HttpClient
import io.ktor.client.features.HttpClientFeature
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.HttpSendPipeline
import io.ktor.client.response.HttpReceivePipeline
import io.ktor.client.response.HttpResponsePipeline
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.Pipeline
import io.ktor.util.pipeline.PipelinePhase
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(ExampleHttpClientFeature::class.java)

/*
 * Example [HttpClientFeature] that adds a request header.
 *
 * A _feature_ is a class receiving a `configuration` (can be an internal class)
 */
class ExampleHttpClientFeature(config: Configuration) {

    // These fields capture the mutable configuration fields
    private val headerName = config.headerName
    private val headerValue = config.headerValue

    // The feature's configuration
    class Configuration {
        lateinit var headerName: String
        lateinit var headerValue: String
    }

    /*
     * A feature needs a companion object, since the feature name class will be used on `install`.
     * This object implements the `HttpClientFeature` interface...
     */
    companion object Feature : HttpClientFeature<Configuration, ExampleHttpClientFeature> {

        override val key: AttributeKey<ExampleHttpClientFeature> = AttributeKey("ExampleHttpClientFeature")

        /*
         * ... with a _prepare_ method that:
         * - Creates a configuration.
         * - Runs the block on it.
         * - And finally creates the feature object with the configuration object.
         */
        override fun prepare(block: Configuration.() -> Unit) = ExampleHttpClientFeature(Configuration().apply(block))

        /*
         * ... and with an _install_ method that intercepts a client pipeline, using the feature object
         * created in the last step.
         */
        override fun install(feature: ExampleHttpClientFeature, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                context.headers.append(feature.headerName, feature.headerValue)
            }

            // Request pipeline
            scope.requestPipeline.show(HttpRequestPipeline.Before)
            scope.requestPipeline.show(HttpRequestPipeline.State)
            scope.requestPipeline.show(HttpRequestPipeline.Transform)
            scope.requestPipeline.show(HttpRequestPipeline.Render)
            scope.requestPipeline.show(HttpRequestPipeline.Send)

            // Send pipeline
            scope.sendPipeline.show(HttpSendPipeline.Before)
            scope.sendPipeline.show(HttpSendPipeline.State)
            scope.sendPipeline.show(HttpSendPipeline.Engine)

            // Receive pipeline
            scope.receivePipeline.show(HttpReceivePipeline.Before)
            scope.receivePipeline.show(HttpReceivePipeline.State)
            scope.receivePipeline.show(HttpReceivePipeline.After)

            // Response pipeline
            scope.responsePipeline.show(HttpResponsePipeline.Receive)
            scope.responsePipeline.show(HttpResponsePipeline.Parse)
            scope.responsePipeline.show(HttpResponsePipeline.Transform)
            scope.responsePipeline.show(HttpResponsePipeline.State)
            scope.responsePipeline.show(HttpResponsePipeline.After)
        }
    }

}

private fun <S : Any, C : Any> Pipeline<S, C>.show(phase: PipelinePhase) {
    val pipeline = this
    intercept(phase) {
        logger.info(
            "Before {}, {}, subject={}, context={}",
            pipeline::class.java.simpleName, phase, subject::class.java.simpleName, context::class.java.simpleName
        )
        proceed()
        logger.info(
            "After {}, {}, subject={}, context={}",
            pipeline::class.java.simpleName, phase, subject::class.java.simpleName, context::class.java.simpleName
        )

    }
}