package io.github.chhch.cre.component.core

import io.github.chhch.commons.Inject
import io.github.chhch.commons.Lifecycle
import io.github.chhch.commons.Scope
import io.github.chhch.cre.component.ComponentContext
import io.github.chhch.cre.component.cache.ComponentCache
import kotlin.reflect.full.findAnnotation

internal object ComponentInjector {

    fun injectComponent(context: ComponentContext) =
            ComponentCache.getContextWithParentClassLoader(context)?.let {
                if (it.isInjectable(context.scope))
                    context.componentInstance::class
                            .setPropertyWithAnnotation<Inject>(context.componentInstance, it.componentInstance)
            }

    fun isInjectable(context: ComponentContext, lifecycle: Lifecycle): Boolean {
        val scopeAnnotation = context.componentInstance::class.findAnnotation<Scope>() ?: return true
        return scopeAnnotation.inject.contains(lifecycle)
    }

}