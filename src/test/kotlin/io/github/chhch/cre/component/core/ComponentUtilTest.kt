package io.github.chhch.cre.component.core

import io.github.chhch.commons.*
import io.github.chhch.cre.component.inject.logger.LoggerImpl
import io.github.chhch.cre.component.inject.observer.EventImpl
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.net.URLClassLoader
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure


class ComponentUtilTest {

    private fun resource(path: String) = javaClass.getResource(path)!!

    @Test
    fun `inject component`() {
        val compositionUrl = resource("/componentComposition-1.0-SNAPSHOT-all.jar")
        val componentToInjectUrl = resource("/componentWithScope-1.0-SNAPSHOT-all.jar")

        val componentToInject = ComponentCreator.newInstance("id", "name", componentToInjectUrl)

        val className = "io.github.chhch.components.composition.ComponentComposition"
        val classLoader = URLClassLoader.newInstance(arrayOf(compositionUrl), componentToInject::class.java.classLoader)
        val clazz = classLoader.loadClass(className).kotlin

        val injectionField = clazz.declaredMemberProperties.single { it.findAnnotation<Inject>() != null }
        assertThat(injectionField.returnType).isEqualTo(componentToInject::class.createType())
        assertThat(injectionField.returnType.jvmErasure::qualifiedName).equals(componentToInject::class.qualifiedName)
        assertThat(injectionField.returnType).isEqualTo(componentToInject::class.createType())

        val component = clazz.constructors.single().call(LoggerImpl("id", "name"))
        if (injectionField is KMutableProperty<*>) {
            injectionField.setter.call(component, componentToInject)
        }

        assertThat(component::class.qualifiedName).isEqualTo(className)
    }

    @Test
    fun `custom type event`() {
        val senderUrl = resource("/customEventSenderComponent-1.0-SNAPSHOT-all.jar")
        val eventSenderComponent = URLClassLoader.newInstance(arrayOf(senderUrl))
                .loadClass("io.github.chhch.components.dataEvent.CustomEventSenderComponent").kotlin
        val annotatedConstructor = eventSenderComponent.findConstructorWithAnnotation<Inject>()
        val eventParameter = annotatedConstructor?.findValueParameterWithType<EventImpl<*>>(KTypeProjection.contravariant(Any::class.starProjectedType))!!
        val eventType = eventParameter.type.arguments.single().type!!
        val qualifier = eventParameter.annotations.single()

        val receiverUrl = resource("/customEventReceiverComponent-1.0-SNAPSHOT-all.jar")
        val eventReceiverComponent = URLClassLoader.newInstance(arrayOf(receiverUrl))
                .loadClass("io.github.chhch.components.dataEvent.CustomEventReceiverComponent").kotlin
        val observerMethod = eventReceiverComponent.findMethodWithAnnotation<Observer>()

        val findParameterWithType = observerMethod?.valueParameters?.singleOrNull {
            (it.annotations.isEmpty() || it.annotations.any { it.annotationClass.qualifiedName == qualifier.annotationClass.qualifiedName }) && it.type.isSupertypeOf(eventType)
        }

        assertThat(findParameterWithType).isNotNull()
    }

    @Test
    fun `inject event`() {
        val pingJarUrl = resource("/pingComponent-1.0-SNAPSHOT-all.jar")
        val pongJarUrl = resource("/pongComponent-1.0-SNAPSHOT-all.jar")

        val pingComponent = URLClassLoader.newInstance(arrayOf(pingJarUrl))
                .loadClass("io.github.chhch.components.ping.PingComponent").kotlin
        val pongComponent = URLClassLoader.newInstance(arrayOf(pongJarUrl))
                .loadClass("io.github.chhch.components.pong.PongComponent").kotlin

        val annotatedConstructor = pingComponent.findConstructorWithAnnotation<Inject>()
        val eventParameter = annotatedConstructor?.findValueParameterWithType<EventImpl<*>>(KTypeProjection.contravariant(Any::class.starProjectedType))

        val loggerParameter = annotatedConstructor?.findValueParameterWithType<Logger>()
        val qualifier = eventParameter?.annotations?.singleOrNull()
        assertThat(annotatedConstructor).isNotNull()
        assertThat(loggerParameter).isNotNull()
        assertThat(eventParameter).isNotNull()

        if (annotatedConstructor != null && eventParameter != null && loggerParameter != null) {
            val parameterMap = mapOf(
                    eventParameter to EventImpl<Any>(qualifier?.annotationClass?.qualifiedName),
                    loggerParameter to LoggerImpl("id", "testComponent")
            )
            val componentInstance = annotatedConstructor.callBy(parameterMap)

            val observerMethod = pongComponent.findMethodWithAnnotation<Observer>()
            val observerMethodParameter = eventParameter.type.arguments.single()

            val findParameterWithType = observerMethod?.valueParameters?.singleOrNull {
                it.type.isSupertypeOf(observerMethodParameter.type!!)
            }
            assertThat(findParameterWithType).isNotNull()

            val pongComponentInstance = pongComponent.findConstructorWithAnnotation<Inject>()!!.call(LoggerImpl("id", "testComponent"), EventImpl<Any>(qualifier?.annotationClass?.qualifiedName))

            Assertions.assertThatCode {
                observerMethod?.callBy(mapOf(
                        observerMethod.instanceParameter!! to pongComponentInstance,
                        observerMethod.findValueParameterWithType<String>()!! to "TEST2"))

                componentInstance::class.invokeMethodWithAnnotation<Start>(componentInstance)
            }.doesNotThrowAnyException()
        }
    }

    @Test
    fun `inject logger in component`() {
        val testJarUrl = resource("/helloComponentWithLogger-1.0-SNAPSHOT-all.jar")

        val className = "io.github.chhch.components.helloLog.HelloComponent"

        val classLoader = URLClassLoader.newInstance(arrayOf(testJarUrl))
        val component = classLoader.loadClass(className).kotlin

        val annotatedConstructor = component.findConstructorWithAnnotation<Inject>()
        val loggerParameter = annotatedConstructor?.findValueParameterWithType<Logger>()

        if (annotatedConstructor != null && loggerParameter != null) {
            Assertions.assertThatCode {
                val parameterMap = mapOf(loggerParameter to LoggerImpl("id", "testComponent"))
                val componentInstance = annotatedConstructor.callBy(parameterMap)
                componentInstance::class.invokeMethodWithAnnotation<Start>(componentInstance)
            }.doesNotThrowAnyException()
        }
    }

}