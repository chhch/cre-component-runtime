package io.github.chhch.cre.component

import io.github.chhch.cre.component.logging.ComponentLog
import java.io.Serializable

object CRELog {
    fun getMessages(id: String) = ComponentLog.getMessages(id)
    fun subscribe(listener: LogListener) = ComponentLog.subscribe(listener)
}

data class ComponentLogMessage(val id: String, val name: String, val message: String) : Serializable

interface LogListener {

    operator fun invoke(logMessage: ComponentLogMessage)

}