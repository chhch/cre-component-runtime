package io.github.chhch.cre.component

import io.github.chhch.commons.Lifecycle
import io.github.chhch.cre.component.cache.ComponentCache
import io.github.chhch.cre.component.logging.ComponentLog
import io.github.chhch.cre.component.persistence.ComponentLogDB
import java.io.Serializable

object CRECache {
    fun findComponent(id: String) = ComponentCache[id]
    fun getAllComponents() = ComponentCache.toSet()
    fun restore() = ComponentLogDB.restore()
}

fun ComponentContext.toDTO() = ComponentDTO(this)
fun Set<ComponentContext>.toDTO() = this.map { it.toDTO() }

data class ComponentDTO(
        val id: String,
        val name: String,
        val state: ComponentState.State,
        val scope: Lifecycle,
        val logMessages: List<String> = ComponentLog.getMessages(id)
)
    : Serializable {
    constructor(context: ComponentContext) : this(context.id, context.name, context.state, context.scope)

}