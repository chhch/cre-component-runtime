package io.github.chhch.cre.component

import io.github.chhch.commons.Lifecycle
import java.util.concurrent.Future

interface ComponentContext : ComponentState {

    val id: String
    val name: String
    val componentInstance: Any
    val classLoader: ClassLoader get() = componentInstance::class.java.classLoader

}

interface ComponentState : ComponentOperation, ComponentObserver, ComponentScope {

    enum class State {
        INITIALIZED, STARTED, STOPPED, UNLOADED
    }

    val state: State

}

interface ComponentOperation {

    /**
     * Starts the component.
     *
     * @return Future of the invoked start method
     * @throws IllegalStateException if a specific state transition is not allowed
     */
    fun start(): Future<*>

    /**
     * Stops the component.
     *
     * Waits until the invoked start method has finished.
     *
     * @return Future of the invoked stop method
     * @throws IllegalStateException if a specific state transition is not allowed
     */
    fun stop(): Future<*>

    /**
     * Unload the component.
     *
     * Blocks until all invoked methods have completed or the timeout occurs.
     *
     * @throws IllegalStateException if a specific state transition is not allowed
     */
    fun unload(): Future<Boolean>

}

interface ComponentObserver {

    fun onEvent(event: Any, qualifier: String?): Future<*>

}

interface ComponentScope {

    var scope: Lifecycle
    fun isInjectable(lifecycle: Lifecycle): Boolean

}