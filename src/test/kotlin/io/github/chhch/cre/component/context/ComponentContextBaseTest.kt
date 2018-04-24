package io.github.chhch.cre.component.context

import io.github.chhch.cre.component.ComponentState
import org.assertj.core.api.Assertions.*
import org.junit.Test
import java.time.Duration
import java.time.Instant

class ComponentContextBaseTest {

    private val testJarUrl = javaClass.getResource("/helloComponent-1.0-SNAPSHOT-all.jar")!!

    @Test
    fun `load component`() {
        val componentContext = ComponentContextBase(testJarUrl)

        assertThat(componentContext).isNotNull()
        assertThat(componentContext.id).isNotBlank()
        assertThat(componentContext.name).isEqualTo("helloComponent-1.0-SNAPSHOT-all")
        assertThat(componentContext.state).isEqualTo(ComponentState.State.INITIALIZED)
    }

    @Test
    fun `unload when component finished`() {
        val componentContext = ComponentContextBase(testJarUrl)

        val start = Instant.now()
        componentContext.apply {
            start().get()
            assertThat(componentContext.state).isEqualTo(ComponentState.State.STARTED)
            unload().get()
        }
        val stop = Instant.now()

        assertThat(Duration.between(start, stop))
                .isBetween(Duration.ofSeconds(14), Duration.ofSeconds(18))
        assertThat(componentContext.state).isEqualTo(ComponentState.State.UNLOADED)
    }

    @Test
    fun `unload running component`() {
        val componentContext = ComponentContextBase(testJarUrl)

        val start = Instant.now()
        componentContext.apply {
            start()
            assertThat(componentContext.state).isEqualTo(ComponentState.State.STARTED)
            unload()
        }
        val stop = Instant.now()

        assertThat(Duration.between(start, stop))
                .isBetween(Duration.ofSeconds(0), Duration.ofSeconds(6))
        assertThat(componentContext.state).isEqualTo(ComponentState.State.UNLOADED)
    }

    @Test
    fun `run components parallel`() {
        val start = Instant.now()
        listOf(ComponentContextBase(testJarUrl), ComponentContextBase(testJarUrl)).parallelStream().forEach {
            it.apply {
                start().get()
                assertThat(it.state).isEqualTo(ComponentState.State.STARTED)
                unload().get()
            }
            assertThat(it.state).isEqualTo(ComponentState.State.UNLOADED)
        }

        val stop = Instant.now()

        assertThat(Duration.between(start, stop))
                .isBetween(Duration.ofSeconds(14), Duration.ofSeconds(22))
    }

    @Test
    fun `get component state`() {
        val componentContext = ComponentContextBase(testJarUrl)
        assertThat(componentContext.state).isEqualTo(ComponentState.State.INITIALIZED)

        componentContext.start()
        assertThat(componentContext.state).isEqualTo(ComponentState.State.STARTED)

        componentContext.stop()
        assertThat(componentContext.state).isEqualTo(ComponentState.State.STOPPED)

        componentContext.unload()
        assertThat(componentContext.state).isEqualTo(ComponentState.State.UNLOADED)
    }

    @Test
    fun `test not supported component transitions`() {
        val componentContext = ComponentContextBase(testJarUrl)

        assertThat(componentContext.state).isEqualTo(ComponentState.State.INITIALIZED)
        assertThatThrownBy { componentContext.stop() }.isInstanceOf(IllegalStateException::class.java)
        assertThatCode { componentContext.start() }.doesNotThrowAnyException()

        assertThat(componentContext.state).isEqualTo(ComponentState.State.STARTED)
        assertThatThrownBy { componentContext.start() }.isInstanceOf(IllegalStateException::class.java)
        assertThatCode { componentContext.stop() }.doesNotThrowAnyException()

        assertThat(componentContext.state).isEqualTo(ComponentState.State.STOPPED)
        assertThatThrownBy { componentContext.stop() }.isInstanceOf(IllegalStateException::class.java)
        assertThatCode { componentContext.unload() }.doesNotThrowAnyException()

        assertThat(componentContext.state).isEqualTo(ComponentState.State.UNLOADED)
        assertThatThrownBy { componentContext.start() }.isInstanceOf(IllegalStateException::class.java)
        assertThatThrownBy { componentContext.stop() }.isInstanceOf(IllegalStateException::class.java)
        assertThatThrownBy { componentContext.unload() }.isInstanceOf(IllegalStateException::class.java)
    }

}