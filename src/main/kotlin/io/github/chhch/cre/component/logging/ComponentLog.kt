package io.github.chhch.cre.component.logging

import io.github.chhch.cre.component.ComponentLogMessage
import io.github.chhch.cre.component.LogListener

internal object ComponentLog {

    private val messages: MutableList<ComponentLogMessage> = mutableListOf()
    private val listeners: MutableList<LogListener> = mutableListOf()

    fun addMessage(logMessage: ComponentLogMessage) {
        messages.add(logMessage)
        listeners.forEach { it(logMessage) }
    }

    fun getMessages(id: String): List<String> {
        return messages.filter { it.id == id }.map { it.message }
    }

    fun subscribe(listener: LogListener) {
        listeners.add(listener)
    }

}