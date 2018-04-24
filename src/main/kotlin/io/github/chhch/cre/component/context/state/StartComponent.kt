package io.github.chhch.cre.component.context.state

import io.github.chhch.commons.Lifecycle
import io.github.chhch.cre.component.ComponentState
import io.github.chhch.cre.component.context.ComponentContextBase
import io.github.chhch.cre.component.core.ComponentInjector

internal class StartComponent(private val context: ComponentContextBase) : ComponentState {

    override val state = ComponentState.State.STARTED
    override var scope: Lifecycle = context.scope

    private val executor = context.executorService
    val task = executor.start()

    init {
        context.componentState = this
    }

    override fun start() = throw IllegalStateException("Component is already started")
    override fun stop() = StopComponent(context).task
    override fun unload() = stop().let { UnloadComponent(context).task }
    override fun onEvent(event: Any, qualifier: String?) = executor.onEvent(event, qualifier)
    override fun isInjectable(lifecycle: Lifecycle): Boolean = ComponentInjector.isInjectable(context, lifecycle)

}