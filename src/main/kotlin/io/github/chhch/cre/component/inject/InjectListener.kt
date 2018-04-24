package io.github.chhch.cre.component.inject

import io.github.chhch.commons.Lifecycle
import io.github.chhch.cre.component.ComponentContext
import io.github.chhch.cre.component.context.ScopeChangeListener
import io.github.chhch.cre.component.core.ComponentInjector

internal class InjectListener : ScopeChangeListener {

    override fun invoke(oldValue: Lifecycle, newValue: Lifecycle, context: ComponentContext) {
        ComponentInjector.injectComponent(context)
    }

}