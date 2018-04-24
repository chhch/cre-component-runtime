package io.github.chhch.cre.component.persistence

import io.github.chhch.cre.component.ComponentContext
import io.github.chhch.cre.component.ComponentDTO
import io.github.chhch.cre.component.ComponentState.State.*
import io.github.chhch.cre.component.loadComponent
import io.github.chhch.cre.component.persistence.LogEntry.Type.*
import java.io.File
import java.io.Serializable
import java.time.LocalDateTime

internal data class LogEntry(val date: LocalDateTime, val dto: ComponentDTO, val type: Type) : Serializable {

    constructor(context: ComponentContext, type: Type) : this(LocalDateTime.now(), ComponentDTO(context), type)

    enum class Type {
        INITIALIZE, STATE_CHANGE, SCOPE_CHANGE
    }
}


internal fun List<LogEntry>.load(path: String = "") {
    val tmp = mutableMapOf<String, ComponentContext>()
    this.forEach {
        when (it.type) {
            INITIALIZE -> tmp[it.dto.id] = File("$path/${it.dto.name}.jar").toURI().toURL().loadComponent()
            SCOPE_CHANGE -> tmp[it.dto.id]?.scope = it.dto.scope
            STATE_CHANGE -> {
                when (it.dto.state) {
                    INITIALIZED -> Unit
                    STARTED -> tmp[it.dto.id]?.start()
                    STOPPED -> tmp[it.dto.id]?.stop()
                    UNLOADED -> tmp[it.dto.id]?.takeIf { it.state != UNLOADED }?.unload()
                }
            }
        }
    }
}