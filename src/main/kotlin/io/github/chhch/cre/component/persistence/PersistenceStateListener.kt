package io.github.chhch.cre.component.persistence

import io.github.chhch.cre.component.ComponentContext
import io.github.chhch.cre.component.context.StateChangeListener
import io.github.chhch.cre.component.ComponentState.State
import io.github.chhch.cre.component.unit

/**
 * Access and control a component.
 *
 * Logs state changes of the component in a db.
 *
 */
internal class PersistenceStateListener : StateChangeListener {

    override operator fun invoke(oldValue: State, newValue: State, context: ComponentContext) =
            ComponentLogDB.addStateChangeEntry(context).unit

}