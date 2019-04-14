package first

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.request.httpMethod
import io.ktor.request.uri
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(ExampleFeature::class.java)

/*
 * A feature is a class receiving a `Configuration` object
 */
class ExampleFeature(config: Configuration) {

    /*
     * Where the `Configuration` can be defined as inner class
     */
    class Configuration {
        var configValue: String = "some-default-value"
        var anotherConfigValue: String = "another-default-value"
        /*
         * The configuration object cannot be mutable because it will be changed by
         * the "configure block"
         */
    }

    /*
     * Typically, the feature constructor will create an immutable copy of the configuration values.
     */
    val configValue = config.configValue
    val anotherConfigValue = config.anotherConfigValue

    /*
     * A feature needs a companion object, since the feature name class will be used on `install`.
     * This object implements the `ApplicationFeature` interface...
     */
    companion object Feature : ApplicationFeature<Application, Configuration, ExampleFeature> {

        override val key = AttributeKey<ExampleFeature>("ExampleFeature")

        /*
         * ... which has an install method that
         * - Run the "configure block" to obtain an initialized `Configuration` object.
         * - Creates the feature instance.
         * - Uses the feature instance to install interceptors into the pipeline.
         */
        override fun install(pipeline: Application, configure: Configuration.() -> Unit): ExampleFeature {
            val mutableConfiguration = Configuration().apply(configure)
            val feature = ExampleFeature(mutableConfiguration)

            pipeline.intercept(ApplicationCallPipeline.Setup) {
                feature.interceptSetup(this)
            }

            pipeline.intercept(ApplicationCallPipeline.Monitoring) {
                feature.interceptMonitoring(this)
            }

            pipeline.intercept(ApplicationCallPipeline.Features) {
                feature.interceptFeatures(this)
            }

            pipeline.intercept(ApplicationCallPipeline.Call) {
                feature.interceptCall(this)
            }

            pipeline.intercept(ApplicationCallPipeline.Fallback) {
                feature.interceptFallback(this)
            }

            return feature
        }
    }

    /*
     * Interceptor methods
     */
    private suspend fun interceptSetup(pipelineContext: PipelineContext<Unit, ApplicationCall>) {
        val request = pipelineContext.context.request
        logger.info("interceptSetup/start: method=${request.httpMethod.value}, uri=${request.uri}")
        pipelineContext.proceed()
        val response = pipelineContext.context.response
        logger.info("interceptSetup/end: method=${request.httpMethod.value}, uri=${request.uri}, " +
                "status=${response.status()}")

    }

    private suspend fun interceptMonitoring(pipelineContext: PipelineContext<Unit, ApplicationCall>) {
        val request = pipelineContext.context.request
        logger.info("interceptMonitoring/start: method=${request.httpMethod.value}, uri=${request.uri}")
        pipelineContext.proceed()
        val response = pipelineContext.context.response
        logger.info("interceptMonitoring/end: method=${request.httpMethod.value}, uri=${request.uri}, " +
                "status=${response.status()}")
    }

    private suspend fun interceptFeatures(pipelineContext: PipelineContext<Unit, ApplicationCall>) {
        val request = pipelineContext.context.request
        logger.info("interceptFeatures/start: method=${request.httpMethod.value}, uri=${request.uri}")
        pipelineContext.proceed()
        val response = pipelineContext.context.response
        logger.info("interceptFeatures/end: method=${request.httpMethod.value}, uri=${request.uri}, " +
                "status=${response.status()}")
    }

    private suspend fun interceptCall(pipelineContext: PipelineContext<Unit, ApplicationCall>) {
        val request = pipelineContext.context.request
        logger.info("interceptCall/start: method=${request.httpMethod.value}, uri=${request.uri}")
        pipelineContext.proceed()
        val response = pipelineContext.context.response
        logger.info("interceptCall/end: method=${request.httpMethod.value}, uri=${request.uri}, " +
                "status=${response.status()}")
    }

    private suspend fun interceptFallback(pipelineContext: PipelineContext<Unit, ApplicationCall>) {
        val request = pipelineContext.context.request
        logger.info("interceptFallback/start: method=${request.httpMethod.value}, uri=${request.uri}")
        pipelineContext.proceed()
        val response = pipelineContext.context.response
        logger.info("interceptFallback/end: method=${request.httpMethod.value}, uri=${request.uri}, " +
                "status=${response.status()}")
    }

    /*
     * Result:
     *
     * [nettyCallPool-4-1] INFO first.ExampleFeature - interceptSetup/start: method=GET, uri=/
     * [nettyCallPool-4-1] INFO first.ExampleFeature - interceptMonitoring/start: method=GET, uri=/
     * [nettyCallPool-4-1] INFO first.ExampleFeature - interceptFeatures/start: method=GET, uri=/
     * [nettyCallPool-4-1] INFO first.ExampleFeature - interceptCall/start: method=GET, uri=/
     * [nettyCallPool-4-1] INFO module - route '/
     * [nettyCallPool-4-1] INFO first.ExampleFeature - interceptFallback/start: method=GET, uri=/
     * [nettyCallPool-4-1] INFO first.ExampleFeature - interceptFallback/end: method=GET, uri=/, status=200 OK
     * [nettyCallPool-4-1] INFO first.ExampleFeature - interceptCall/end: method=GET, uri=/, status=200 OK
     * [nettyCallPool-4-1] INFO first.ExampleFeature - interceptFeatures/end: method=GET, uri=/, status=200 OK
     * [nettyCallPool-4-1] INFO first.ExampleFeature - interceptMonitoring/end: method=GET, uri=/, status=200 OK
     * [nettyCallPool-4-1] INFO first.ExampleFeature - interceptSetup/end: method=GET, uri=/, status=200 OK
     */

}