package io.github.chhch.cre.component.context.state

import io.github.chhch.commons.Lifecycle
import io.github.chhch.cre.component.ComponentState
import io.github.chhch.cre.component.context.ComponentContextBase
import io.github.chhch.cre.component.core.ComponentInjector

internal class InitializeComponent(private val context: ComponentContextBase) : ComponentState {

    override val state = ComponentState.State.INITIALIZED
    override var scope: Lifecycle = Lifecycle.UNDER_TEST

    private val executor = context.executorService

    override fun start() = StartComponent(context).task
    override fun stop() = throw IllegalStateException("Initialized to Stop is not allowed")
    override fun unload() = UnloadComponent(context).task
    override fun onEvent(event: Any, qualifier: String?) = executor.onEvent(event, qualifier)
    override fun isInjectable(lifecycle: Lifecycle): Boolean = ComponentInjector.isInjectable(context, lifecycle)

}