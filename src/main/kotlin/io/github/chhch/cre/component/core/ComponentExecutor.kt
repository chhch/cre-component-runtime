package io.github.chhch.cre.component.core

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.chhch.commons.Observer
import io.github.chhch.commons.Start
import io.github.chhch.commons.Stop
import io.github.chhch.cre.component.ComponentObserver
import io.github.chhch.cre.component.ComponentOperation
import java.net.URL
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.concurrent.CompletableFuture.supplyAsync
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.valueParameters

internal class ComponentExecutor(private val componentInstance: Any) : ComponentOperation, ComponentObserver {

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    override fun start(): Future<*> =
            executor.submit { componentInstance::class.invokeMethodWithAnnotation<Start>(componentInstance) }

    override fun stop(): Future<*> =
            executor.submit { componentInstance::class.invokeMethodWithAnnotation<Stop>(componentInstance) }

    override fun unload(): Future<Boolean> = supplyAsync {
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.SECONDS)
    }

    override fun onEvent(event: Any, qualifier: String?): Future<*> =
            findRunnableObserverMethod(event, qualifier)?.let { executor.submit(it) } ?: completedFuture(Unit)

    private fun findRunnableObserverMethod(event: Any, qualifier: String?) =
            componentInstance::class
                    .findMethodWithAnnotation<Observer>()
                    .takeIf {
                        it != null
                                && it.validParameter(event::class.createType())
                                && it.validAnnotation(qualifier)
                    }
                    ?.let {
                        val observerEventParameter = it.findParameterClass()
                        val data: Any =
                                if (event::class.java.classLoader != observerEventParameter?.classLoader)
                                    transfer(event, observerEventParameter)
                                else
                                    event

                        Runnable {
                            it.call(componentInstance, data)
                        }
                    }

    private fun KFunction<*>.validParameter(type: KType) =
            valueParameters.singleOrNull { it.type == type } != null

    private fun KFunction<*>.validAnnotation(qualifier: String?) =
            qualifier == null || findAnnotationByName(qualifier) != null

    private fun transfer(source: Any, target: Class<out Any>?): Any {
        val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())
        val tmp = mapper.writeValueAsBytes(source)
        return mapper.readValue(tmp, target)
    }

}