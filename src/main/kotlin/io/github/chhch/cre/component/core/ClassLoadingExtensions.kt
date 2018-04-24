package io.github.chhch.cre.component.core

import java.io.FileNotFoundException
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.JarFile
import kotlin.reflect.*

/**
 * Loads the main class from a jar file with [URLClassLoader].
 *
 * @return loaded class
 */
internal fun URL.loadClassFromJar(parent: ClassLoader? = BaseClassLoader()): KClass<out Any> {
    val mainClass = JarFile(file).getMainClass()
    val classLoader = URLClassLoader.newInstance(arrayOf(this), parent)
    return classLoader.loadClass(mainClass).kotlin
}

/**
 * Loads the main class from a jar file manifest.
 *
 * @return name of the main class
 */
internal fun JarFile.getMainClass(): String {
    val manifestAttributes = this.manifest?.mainAttributes
            ?: throw  FileNotFoundException("Manifest not found.")

    val mainClass = manifestAttributes.getValue("Main-Class")
            ?: throw ClassNotFoundException("Failed to load Main-Class manifest attribute.")

    return mainClass.removeSuffix("Kt")
}

internal class BaseClassLoader : ClassLoader() {

    /**
     * Matches _most_ of the classes from java and kotlin
     * https://docs.oracle.com/javase/8/docs/api/overview-frame.html
     * https://kotlinlang.org/api/latest/jvm/stdlib/index.html
     */
    private val loadClassesStartWith =
            listOf("io.github.chhch.commons.", "kotlin.", "java.", "javax.", "sun.")

    override fun loadClass(name: String?, resolve: Boolean): Class<*> {
        if (loadClassesStartWith.any { name?.startsWith(it) == true })
            return super.loadClass(name, resolve)
        throw ClassNotFoundException(name)
    }
}