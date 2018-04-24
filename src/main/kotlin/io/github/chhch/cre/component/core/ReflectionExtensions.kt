package io.github.chhch.cre.component.core

import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

/**
 * invokes the method with the given Annotation
 */
internal inline fun <reified T : Annotation> KClass<*>.invokeMethodWithAnnotation(component: Any) =
        findMethodWithAnnotation<T>()?.call(component)

/**
 * invokes the setter of the property with the given Annotation
 */
internal inline fun <reified T : Annotation> KClass<*>.setPropertyWithAnnotation(component: Any, value: Any) =
        (findPropertyWithAnnotation<T>() as? KMutableProperty<*>)?.setter?.call(component, value)

/**
 * @return the method with the given Annotation, or `null` if there's no such method or more than one.
 */
internal inline fun <reified T : Annotation> KClass<*>.findMethodWithAnnotation() =
        declaredMemberFunctions.singleOrNull { it.findAnnotation<T>() != null }

/**
 * @return the constructor with the given Annotation, or `null` if there's no such constructor or more than one.
 */
internal inline fun <reified T : Annotation> KClass<*>.findConstructorWithAnnotation() =
        constructors.singleOrNull { it.findAnnotation<T>() != null }

/**
 * @return the property with the given Annotation, or `null` if there's no such property or more than one.
 */
internal inline fun <reified T : Annotation> KClass<*>.findPropertyWithAnnotation() =
        declaredMemberProperties.singleOrNull { it.findAnnotation<T>() != null }

/**
 * @return the name of the Annotation, or `null` if there's no annotation or more than one.
 */
internal fun KParameter.findAnnotationName() = annotations.singleOrNull()?.annotationClass?.qualifiedName

/**
 * @return the annotation with the given name, or `null` if there's no such annotation or more than one.
 */
internal fun KFunction<*>.findAnnotationByName(name: String) =
        annotations.singleOrNull { it.annotationClass.qualifiedName == name }

internal fun KFunction<*>.findParameterClass() = valueParameters.singleOrNull()?.type?.jvmErasure?.java
/**
 * @return the parameter with the given type, or `null` if there's no such method or more than one.
 */
internal inline fun <reified T : Any> KFunction<*>.findValueParameterWithType(vararg arguments: KTypeProjection = emptyArray()) =
        valueParameters.singleOrNull { it.type.isSupertypeOf(T::class.createType(arguments.toList())) }

internal fun KTypeProjection.Companion.inAny() = contravariant(Any::class.createType())

internal inline fun <reified T : Annotation> KClass<*>.checkForSinglePublicMethodWithAnnotation() {
    val annotatedMethod = findMethodWithAnnotation<T>()
    when {
        annotatedMethod == null ->
            throw NoSuchMethodException("Exactly one method have to be annotated with @${T::class.simpleName}.")
        annotatedMethod.visibility != KVisibility.PUBLIC ->
            throw IllegalCallableAccessException(IllegalAccessException("${T::class.simpleName} annotated method must be public."))
    }
}