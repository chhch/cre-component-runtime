package io.github.chhch.cre.component.context.state

import io.github.chhch.commons.Lifecycle
import io.github.chhch.cre.component.ComponentState
import io.github.chhch.cre.component.context.ComponentContextBase

internal class UnloadComponent(context: ComponentContextBase) : ComponentState {

    override val state = ComponentState.State.UNLOADED
    override var scope: Lifecycle = context.scope
        set(value) = throw IllegalStateException("Component is already unloaded")

    private val executor = context.executorService
    val task = executor.unload()

    init {
        context.componentState = this
    }

    override fun start() = throw IllegalStateException("Unload to Start is not allowed")
    override fun stop() = throw IllegalStateException("Unload to Stop is not allowed")
    override fun unload() = throw IllegalStateException("Component is already unloaded")
    override fun onEvent(event: Any, qualifier: String?) =
            throw IllegalStateException("Component is already unloaded")
    override fun isInjectable(lifecycle: Lifecycle): Boolean = false

}
