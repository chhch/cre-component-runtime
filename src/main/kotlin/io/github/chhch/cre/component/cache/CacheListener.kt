package io.github.chhch.cre.component.cache

import io.github.chhch.cre.component.ComponentContext
import io.github.chhch.cre.component.ComponentState.State
import io.github.chhch.cre.component.ComponentState.State.*
import io.github.chhch.cre.component.context.StateChangeListener

/**
 *
 * Only components which are cached can receive events
 * Only components which are cached can be injected in other components
 *
 */
internal class CacheListener : StateChangeListener {

    override operator fun invoke(oldValue: State, newValue: State, context: ComponentContext) =
            when (newValue) {
                INITIALIZED, STARTED, STOPPED -> Unit
                UNLOADED -> {
                    ComponentCache.remove(context)
                    ComponentCache.getContextWithDependedLoader(context).forEach { it.unload() }
                }
            }
}