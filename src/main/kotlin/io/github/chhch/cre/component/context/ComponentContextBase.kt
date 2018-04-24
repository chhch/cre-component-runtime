package io.github.chhch.cre.component.context

import io.github.chhch.commons.Lifecycle
import io.github.chhch.cre.component.ComponentContext
import io.github.chhch.cre.component.ComponentState
import io.github.chhch.cre.component.context.state.InitializeComponent
import io.github.chhch.cre.component.core.ComponentCreator
import io.github.chhch.cre.component.core.ComponentExecutor
import io.github.chhch.cre.component.getFilename
import java.net.URL
import java.util.*
import kotlin.properties.Delegates

/**
 * Access and control a component.
 *
 * Every component uses its own class loader. The start and stop methods are invoked in separate
 * threads, but the invoke sequence will be retained. The components stop method will not
 * invoked before the start method has finished.
 *
 * @property jar of the components jar file
 * @property name of the component
 * @see InitializeComponent for Exceptions which could thrown
 */
internal class ComponentContextBase(
        private val jar: URL,
        private val stateListener: List<StateChangeListener> = emptyList(),
        private val scopeListener: List<ScopeChangeListener> = emptyList()
) : ComponentContext {

    override val id: String = UUID.randomUUID().toString()
    override val name: String = jar.getFilename()

    override val componentInstance = ComponentCreator.newInstance(id, name, jar)
    internal val executorService = ComponentExecutor(componentInstance)
    internal var componentState: ComponentState by Delegates.observable(InitializeComponent(this) as ComponentState) {
        _, oldValue, newValue ->
        stateListener.forEach { it(oldValue.state, newValue.state, this) }
    }
    override val state: ComponentState.State get() = componentState.state
    override var scope: Lifecycle by Delegates.observable(componentState.scope) {
        _, oldValue, newValue ->
        scopeListener.forEach { it(oldValue, newValue, this) }
    }

    override fun start() = componentState.start()
    override fun stop() = componentState.stop()
    override fun unload() = componentState.unload()
    override fun onEvent(event: Any, qualifier: String?) = componentState.onEvent(event, qualifier)
    override fun isInjectable(lifecycle: Lifecycle) = componentState.isInjectable(lifecycle)

}