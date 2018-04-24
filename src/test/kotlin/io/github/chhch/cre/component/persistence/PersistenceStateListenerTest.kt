package io.github.chhch.cre.component.persistence

import io.github.chhch.cre.component.cache.ComponentCache
import io.github.chhch.cre.component.ComponentState
import io.github.chhch.cre.component.isolatedComponent
import io.github.chhch.cre.component.loadComponent
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.BeforeClass
import org.junit.Test

class PersistenceStateListenerTest {

    private val testJarUrl = javaClass.getResource("/helloComponentWithLogger-1.0-SNAPSHOT-all.jar")!!

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            ComponentCache.clear()
            ComponentLogDB.clear()
        }
    }

    @After
    fun cleanupTest() {
        ComponentCache.clear()
        ComponentLogDB.clear()
    }

    @Test
    fun `load component with logger injection`() {
        val persistComponent = testJarUrl.isolatedComponent()

        persistComponent.apply {
            start().get()
            assertThat(persistComponent.state).isEqualTo(ComponentState.State.STARTED)
            unload()
        }

        assertThat(persistComponent.state).isEqualTo(ComponentState.State.UNLOADED)
    }

    @Test
    fun `state Log`() {
        val persistComponent = testJarUrl.loadComponent()

        assertThat(ComponentLogDB.filter { it.dto.id == persistComponent.id }).hasSize(1)
        assertThat(ComponentCache).contains(persistComponent)
    }

    @Test
    fun `persistent context`() {
        val persistComponent = testJarUrl.isolatedComponent()

        persistComponent.apply {
            start().get()
            assertThat(persistComponent.state).isEqualTo(ComponentState.State.STARTED)
            unload()
        }

        assertThat(persistComponent.state).isEqualTo(ComponentState.State.UNLOADED)
    }

    @Test
    fun `persistent context state`() {
        val persistComponent = testJarUrl.isolatedComponent()

        persistComponent.apply {
            start().get()
            assertThat(persistComponent.state).isEqualTo(ComponentState.State.STARTED)
            unload()
        }

        assertThat(persistComponent.state).isEqualTo(ComponentState.State.UNLOADED)

        val stateLogs = ComponentLogDB
                .filter { it.dto.id == persistComponent.id }
                .sortedBy { it.date }

        assertThat(stateLogs).hasSize(4)
        assertThat(stateLogs[0].dto.state).isEqualTo(ComponentState.State.INITIALIZED)
        assertThat(stateLogs[1].dto.state).isEqualTo(ComponentState.State.STARTED)
        assertThat(stateLogs[2].dto.state).isEqualTo(ComponentState.State.STOPPED)
        assertThat(stateLogs[3].dto.state).isEqualTo(ComponentState.State.UNLOADED)

        assertThat(stateLogs[0].date)
                .isBefore(stateLogs[1].date)
                .isBefore(stateLogs[2].date)
                .isBefore(stateLogs[3].date)
    }

}