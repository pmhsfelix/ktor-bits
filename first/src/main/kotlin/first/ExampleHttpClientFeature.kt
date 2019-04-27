package first

import io.ktor.client.HttpClient
import io.ktor.client.features.HttpClientFeature
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.util.AttributeKey

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
        }
    }
}