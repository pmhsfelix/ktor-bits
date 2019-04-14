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

            pipeline.intercept(ApplicationCallPipeline.Call) {
                feature.interceptCall(this)
            }

            return feature
        }
    }

    /*
     * This method intercepts the `Call` phase
     */
    private fun interceptCall(pipelineContext: PipelineContext<Unit, ApplicationCall>) {
        val request = pipelineContext.context.request
        logger.info("interceptCall: method=${request.httpMethod.value}, uri=${request.uri}")
    }

}