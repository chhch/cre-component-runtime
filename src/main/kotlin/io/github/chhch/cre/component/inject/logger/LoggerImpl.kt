package io.github.chhch.cre.component.inject.logger

import io.github.chhch.commons.Logger
import io.github.chhch.cre.component.ComponentLogMessage
import io.github.chhch.cre.component.logging.ComponentLog
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal class LoggerImpl(private val id: String, private val name: String) : Logger {

    private val time get() = LocalTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

    override fun sendLog(message: String) {
        println("++++ $time - Runtime-Log [$name]: $message ")
        ComponentLog.addMessage(ComponentLogMessage(id, name, message))
    }

}

