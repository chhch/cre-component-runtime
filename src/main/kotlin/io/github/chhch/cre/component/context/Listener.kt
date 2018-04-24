package io.github.chhch.cre.component.context

import io.github.chhch.commons.Lifecycle
import io.github.chhch.cre.component.ComponentContext
import io.github.chhch.cre.component.ComponentState

internal interface ScopeChangeListener {

    operator fun invoke(
            oldValue: Lifecycle,
            newValue: Lifecycle,
            context: ComponentContext
    )

}

internal interface StateChangeListener {

    operator fun invoke(
            oldValue: ComponentState.State,
            newValue: ComponentState.State,
            context: ComponentContext
    )

}