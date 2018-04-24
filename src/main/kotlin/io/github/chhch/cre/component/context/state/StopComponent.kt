package io.github.chhch.cre.component.context.state

import io.github.chhch.commons.Lifecycle
import io.github.chhch.cre.component.ComponentState
import io.github.chhch.cre.component.context.ComponentContextBase
import io.github.chhch.cre.component.core.ComponentInjector

internal class StopComponent(private val context: ComponentContextBase) : ComponentState {

    override val state = ComponentState.State.STOPPED
    override var scope: Lifecycle = context.scope

    private val executor = context.executorService
    val task = executor.stop()

    init {
        context.componentState = this
    }

    override fun start() = StartComponent(context).task
    override fun stop() = throw IllegalStateException("Component is already stopped")
    override fun unload() = UnloadComponent(context).task
    override fun onEvent(event: Any, qualifier: String?) = executor.onEvent(context, qualifier)
    override fun isInjectable(lifecycle: Lifecycle): Boolean = ComponentInjector.isInjectable(context, lifecycle)

}
