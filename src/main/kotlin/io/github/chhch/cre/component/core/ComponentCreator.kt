package io.github.chhch.cre.component.core

import io.github.chhch.commons.*
import io.github.chhch.cre.component.cache.ComponentCache
import io.github.chhch.cre.component.ComponentContext
import io.github.chhch.cre.component.getFilename
import io.github.chhch.cre.component.inject.logger.LoggerImpl
import io.github.chhch.cre.component.inject.observer.EventImpl
import java.io.IOException
import java.net.URL
import kotlin.reflect.*
import kotlin.reflect.full.IllegalCallableAccessException
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation

/**
 * @throws IOException if an I/O error has occurred
 * @throws SecurityException if access to the jar file is denied by the SecurityManager
 * @throws IllegalStateException may be thrown if the jar file has been closed
 * @throws IllegalArgumentException if the jar manifest have no _Main-Class_ attribute
 * @throws ClassNotFoundException if the _Main-Class_ was not found
 * @throws IllegalAccessException if the class or its null-ary constructor is not accessible.
 * @throws InstantiationException if this {@code Class} represents an abstract class,
 *          an interface, an array class, a primitive type, or void;
 *          or if the class has no null-ary constructor;
 *          or if the instantiation fails for some other reason.
 * @throws  ExceptionInInitializerError if the initialization provoked by this method fails.
 * @throws  SecurityException
 *          If a security manager, <i>s</i>, is present and
 *          the caller's class loader is not the same as or an
 *          ancestor of the class loader for the current class and
 *          invocation of {@link SecurityManager#checkPackageAccess
 *          s.checkPackageAccess()} denies access to the package
 *          of this class.
 * @throws NoSuchMethodException if the component has either no @[Start] or @[Stop] annotated method
 * @throws IllegalCallableAccessException if the components main class, constructor, @[Start] or @[Stop] annotated methods are not accessible
 */
internal object ComponentCreator {

    fun newInstance(id: String, name: String, jarURL: URL): Any {
        var clazz = jarURL.loadClassFromJar()
        clazz.checkForSinglePublicMethodWithAnnotation<Start>()
        clazz.checkForSinglePublicMethodWithAnnotation<Stop>()

        val injectPropertyAndValue =
                clazz.findPropertyWithAnnotationAndCorrespondingCachedComponent<Inject>()

        if (injectPropertyAndValue.exists()) {
            clazz = jarURL.loadClassFromJar(injectPropertyAndValue.valueClassLoader)
        }

        val annotatedConstructor = clazz.findConstructorWithAnnotation<Inject>()
        val annotatedConstructorParameter = initializeParameter(id, name, annotatedConstructor)

        return (annotatedConstructor?.callBy(annotatedConstructorParameter) ?: clazz.createInstance())
    }

    private inline fun <reified T : Annotation> KClass<*>.findPropertyWithAnnotationAndCorrespondingCachedComponent(): PropertyValuePair {
        val property = this.findPropertyWithAnnotation<T>() as? KMutableProperty<*>
        val context = ComponentCache
                .singleOrNull { it.componentInstance::class.createType() == property?.returnType }

        return PropertyValuePair(property, context)
    }

    private fun initializeParameter(id: String, name: String, constructor: KFunction<Any>?):
            MutableMap<KParameter, Any> {
        val annotatedConstructorParameter = mutableMapOf<KParameter, Any>()

        constructor
                ?.findValueParameterWithType<Logger>()
                ?.let { annotatedConstructorParameter += it to LoggerImpl(id, name) }

        constructor
                ?.findValueParameterWithType<EventImpl<*>>(KTypeProjection.inAny())
                ?.let { annotatedConstructorParameter += it to EventImpl<Any>(it.findAnnotationName()) }

        return annotatedConstructorParameter
    }

    private data class PropertyValuePair(val property: KMutableProperty<*>?, val context: ComponentContext?) {
        val componentInstance = context?.componentInstance
        val valueClassLoader: ClassLoader? =
                if (componentInstance != null) componentInstance::class.java.classLoader
                else null

        fun exists(): Boolean = property != null && context != null
    }

}