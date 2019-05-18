package com.pedrofelix.ktor.bits

import io.ktor.util.pipeline.Pipeline
import io.ktor.util.pipeline.PipelinePhase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

typealias TheSubject = String
typealias TheContext = MutableList<String>

class PipelineTests {

    @Test
    fun `what is a pipeline?`() {
        runBlocking {

            // A pipeline is typed by
            // - `TContext` - the type of the contextual object, available on all pipeline "elements"
            // - `TSubject` - the type of the pipeline input and output
            val pipeline0 = Pipeline<TheSubject, TheContext>()

            val context: TheContext = mutableListOf()
            val input: TheSubject = "the input"
            val result: TheSubject = pipeline0.execute(context, input)

            // an empty pipeline outputs it input
            assertEquals(input, result)
        }
    }

    private val context: TheContext = mutableListOf()
    private val input: TheSubject = "the input"

    @Test
    fun `a pipeline is composed by phases with interceptors`() {
        runBlocking {

            val phase0 = PipelinePhase("phase0")
            val phase1 = PipelinePhase("phase1")
            val pipeline = Pipeline<TheSubject, TheContext>(phase0, phase1)

            // a pipeline can have interceptors associated to a phase
            pipeline.intercept(phase1) { interceptorInput: TheSubject ->
                println("The interceptor input has type subject `TSubject`: $interceptorInput")
                println("The interceptor receiver has a property with the pipeline context: $context")
                println("The interceptor receiver also has a property with the phase input: $subject")

                // an interceptor can proceed to the next phase with a different subject
                proceedWith(interceptorInput.toUpperCase())
            }

            // the pipeline result is the value produced by the last interceptor
            assertEquals(input.toUpperCase(), pipeline.execute(context, input))

            pipeline.intercept(phase0) {
                println("second interceptor input = $subject")
                // by default, the same subject is passed to the next interceptor
            }
            assertEquals(input.toUpperCase(), pipeline.execute(context, input))
        }
    }

    private val phase0 = PipelinePhase("phase0")
    private val phase1 = PipelinePhase("phase1")
    private val pipeline = Pipeline<TheSubject, TheContext>(phase0, phase1)

    @Test
    fun `interceptor ordering`() {
        runBlocking {
            // The interceptors are run sorted by phases
            pipeline.intercept(phase1) {
                context.add("first phase1 interceptor")
            }
            pipeline.intercept(phase0) {
                context.add("first phase0 interceptor")
            }
            pipeline.intercept(phase0) {
                context.add("second phase0 interceptor")
            }
            val context = mutableListOf<String>()
            pipeline.execute(context, "input")

            val expected = listOf(
                "first phase0 interceptor",
                "second phase0 interceptor",
                "first phase1 interceptor"
            )
            assertEquals(expected, context)
        }
    }

    @Test
    fun `each interceptor can change the value passed on to the next interceptor`() {
        runBlocking {

            pipeline.intercept(phase0) {
                proceedWith(subject + '1')
            }
            pipeline.intercept(phase0) {
                proceedWith(subject + '2')
            }
            pipeline.intercept(phase0) {
                proceedWith(subject + '3')
            }

            assertEquals("input123", pipeline.execute(context, "input"))
        }
    }

    @Test
    fun `the proceed method returns the pipeline end result`() {
        runBlocking {

            pipeline.intercept(phase0) {
                val res = proceedWith(subject + '1')
                assertEquals(res, "input123")
            }
            pipeline.intercept(phase0) {
                val res = proceedWith(subject + '2')
                assertEquals(res, "input123")
            }
            pipeline.intercept(phase0) {
                val res = proceedWith(subject + '3')
                assertEquals(res, "input123")
            }

            assertEquals("input123", pipeline.execute(context, "input"))
        }
    }

    @Test
    fun `an interceptor can terminate the pipeline execution`() {
        runBlocking {

            pipeline.intercept(phase0) {
                proceedWith(subject + '1')
            }
            pipeline.intercept(phase0) {
                finish()
            }
            pipeline.intercept(phase0) {
                proceedWith(subject + '3')
            }

            assertEquals("input1", pipeline.execute(context, "input"))
        }
    }

    @Test
    fun `an interceptor can run code after the proceed returns`() {
        runBlocking {

            pipeline.intercept(phase0) {
                context.add("start first interceptor")
                proceedWith(subject + '1')
                context.add("end first interceptor")
            }
            pipeline.intercept(phase0) {
                context.add("start second interceptor")
                proceedWith(subject + '2')
                context.add("end second interceptor")
            }
            pipeline.intercept(phase0) {
                context.add("start third interceptor")
                proceedWith(subject + '3')
                context.add("end third interceptor")
            }

            assertEquals("input123", pipeline.execute(context, "input"))

            val expectedContext = listOf(
                "start first interceptor",
                "start second interceptor",
                "start third interceptor",
                "end third interceptor",
                "end second interceptor",
                "end first interceptor"
            )
            assertEquals(expectedContext, context);
        }
    }

    @Test
    fun `apparently, proceed can be called multiple times, however interceptors run only once?`() {
        runBlocking {
            pipeline.intercept(phase0) {
                try {
                    proceed()
                } catch (e: Exception) {
                    proceedWith(e.message!!)
                }
            }
            pipeline.intercept(phase1) {
                throw Exception("error on second interceptor")
            }
            pipeline.intercept(phase1) {
                proceedWith(subject.toUpperCase())
            }

            assertEquals("ERROR ON SECOND INTERCEPTOR", pipeline.execute(context, "input"))
        }
    }

    @Test
    fun `retry will not run subsequent failed interceptors again?`() {
        runBlocking {
            pipeline.intercept(phase0) {
                println(proceed())
            }
            pipeline.intercept(phase0) {
                proceedWith("a")
                proceedWith("b")
                proceedWith("c")
            }
            pipeline.intercept(phase1) {
                context.add("second interceptor called with $subject")
            }
            val result = pipeline.execute(context, "input")
            assertEquals("c", result)
            val expectedContext = listOf(
                "second interceptor called with a"
            )
            assertEquals(expectedContext, context)
        }
    }
}