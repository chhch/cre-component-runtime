package io.github.chhch.cre.component.inject.observer

import io.github.chhch.commons.Event
import io.github.chhch.cre.component.cache.ComponentCache

internal class EventImpl<in T : Any>(private val qualifier: String?) : Event<T> {

    override fun fire(event: T) = ComponentCache.forEach {
        it.onEvent(event, qualifier)
    }

}