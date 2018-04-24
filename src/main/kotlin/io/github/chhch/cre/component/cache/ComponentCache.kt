package io.github.chhch.cre.component.cache

import io.github.chhch.cre.component.ComponentContext

/**
 * The Cache maps ComponentContext to its dependencies
 *
 * For example:
 *   B depends on A, when the parent class loader in B is the class loader from A.
 *   This is the case, when B injected an instance from A.
 */
internal object ComponentCache : MutableSet<ComponentContext> by mutableSetOf() {

    fun getContextWithDependedLoader(injected: ComponentContext) =
            filter { it.classLoader.parent == injected.classLoader }

    fun getContextWithParentClassLoader(composition: ComponentContext) =
            singleOrNull { it.classLoader == composition.classLoader.parent }

    operator fun get(id: String) = find { it.id == id }

}