package io.github.chhch.cre.component.persistence

import io.github.chhch.cre.component.ComponentContext
import io.github.chhch.cre.component.FileHandler.COMPONENT_PATH
import io.github.chhch.cre.component.persistence.LogEntry.Type
import org.mapdb.DBMaker

@Suppress("UNCHECKED_CAST")
internal object ComponentLogDB : MutableSet<LogEntry> by DBMaker
        .fileDB("configuration.db")
        .closeOnJvmShutdown()
        .make()
        .hashSet("componentLog")
        .createOrOpen() as MutableSet<LogEntry> {

    val log get() = this.sortedBy { it.date }

    fun addInitializeLogEntry(context: ComponentContext) = addLogEntry(context, Type.INITIALIZE)
    fun addStateChangeEntry(context: ComponentContext) = addLogEntry(context, Type.STATE_CHANGE)
    fun addScopeChangeEntry(context: ComponentContext) = addLogEntry(context, Type.SCOPE_CHANGE)
    private fun addLogEntry(context: ComponentContext, type: Type) = add(LogEntry(context, type))

    fun restore(path: String = COMPONENT_PATH) {
        val tmp = log
        clear()
        tmp.load(path)
    }

}