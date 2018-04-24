package io.github.chhch.cre.component.persistence

import io.github.chhch.commons.Lifecycle
import io.github.chhch.cre.component.ComponentContext
import io.github.chhch.cre.component.context.ScopeChangeListener
import io.github.chhch.cre.component.unit

internal class PersistenceScopeListener : ScopeChangeListener {

    override fun invoke(oldValue: Lifecycle, newValue: Lifecycle, context: ComponentContext) =
            ComponentLogDB.addScopeChangeEntry(context).unit

}