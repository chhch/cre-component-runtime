package io.github.chhch.cre.component.persistence

import io.github.chhch.cre.component.ComponentState
import io.github.chhch.cre.component.cache.ComponentCache
import io.github.chhch.cre.component.isolatedComponent
import io.github.chhch.cre.component.loadComponent
import io.github.chhch.cre.component.temporaryIsolatedComponent
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.BeforeClass
import org.junit.Test
import java.net.URL

class ComponentLogDBTest {

    private val testJarUrl = javaClass.getResource("/helloComponent-1.0-SNAPSHOT-all.jar")!!

    private val urls: Pair<URL, URL>
        get() {
            val toInjectUrl = javaClass.getResource("/componentWithScope-1.0-SNAPSHOT-all.jar")
            val compositionUrl = javaClass.getResource("/componentComposition-1.0-SNAPSHOT-all.jar")
            return Pair(toInjectUrl, compositionUrl)
        }

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
    fun `load components log from db`() {

        ComponentLogDB.clear()

        val component1 = testJarUrl.isolatedComponent()
        component1.start()
        val component2 = testJarUrl.isolatedComponent()
        component1.stop()
        val component3 = testJarUrl.isolatedComponent()
        component2.start()
        component3.start()
        component1.unload()
        component3.unload()
        component2.stop()
        component2.unload()

        val log = ComponentLogDB.log

        assertThat(log).hasSize(12)
        assertThat(log[0].dto.id).isEqualTo(component1.id)
        assertThat(log[0].dto.state).isEqualTo(ComponentState.State.INITIALIZED)
        assertThat(log[1].dto.id).isEqualTo(component1.id)
        assertThat(log[1].dto.state).isEqualTo(ComponentState.State.STARTED)
        assertThat(log[2].dto.id).isEqualTo(component2.id)
        assertThat(log[2].dto.state).isEqualTo(ComponentState.State.INITIALIZED)
        assertThat(log[3].dto.id).isEqualTo(component1.id)
        assertThat(log[3].dto.state).isEqualTo(ComponentState.State.STOPPED)
        assertThat(log[4].dto.id).isEqualTo(component3.id)
        assertThat(log[4].dto.state).isEqualTo(ComponentState.State.INITIALIZED)
        assertThat(log[5].dto.id).isEqualTo(component2.id)
        assertThat(log[5].dto.state).isEqualTo(ComponentState.State.STARTED)
        assertThat(log[6].dto.id).isEqualTo(component3.id)
        assertThat(log[6].dto.state).isEqualTo(ComponentState.State.STARTED)
        assertThat(log[7].dto.id).isEqualTo(component1.id)
        assertThat(log[7].dto.state).isEqualTo(ComponentState.State.UNLOADED)
        assertThat(log[8].dto.id).isEqualTo(component3.id)
        assertThat(log[8].dto.state).isEqualTo(ComponentState.State.STOPPED)
        assertThat(log[9].dto.id).isEqualTo(component3.id)
        assertThat(log[9].dto.state).isEqualTo(ComponentState.State.UNLOADED)
        assertThat(log[10].dto.id).isEqualTo(component2.id)
        assertThat(log[10].dto.state).isEqualTo(ComponentState.State.STOPPED)
        assertThat(log[11].dto.id).isEqualTo(component2.id)
        assertThat(log[11].dto.state).isEqualTo(ComponentState.State.UNLOADED)
    }

    @Test
    fun `restore components from log`() {
        val component1 = javaClass.getResource("/helloComponent-1.0-SNAPSHOT-all.jar").loadComponent()
        component1.start()
        val component2 = javaClass.getResource("/helloComponent-1.0-SNAPSHOT-all.jar").loadComponent()
        component1.stop()
        val component3 = javaClass.getResource("/helloComponent-1.0-SNAPSHOT-all.jar").loadComponent()
        component2.start()
        component3.start()
        component2.stop()
        component2.unload()

        val log = ComponentLogDB.log

        assertThat(log).hasSize(9)
        assertThat(log[0].dto.id).isEqualTo(component1.id)
        assertThat(log[0].dto.state).isEqualTo(ComponentState.State.INITIALIZED)
        assertThat(log[1].dto.id).isEqualTo(component1.id)
        assertThat(log[1].dto.state).isEqualTo(ComponentState.State.STARTED)
        assertThat(log[2].dto.id).isEqualTo(component2.id)
        assertThat(log[2].dto.state).isEqualTo(ComponentState.State.INITIALIZED)
        assertThat(log[3].dto.id).isEqualTo(component1.id)
        assertThat(log[3].dto.state).isEqualTo(ComponentState.State.STOPPED)
        assertThat(log[4].dto.id).isEqualTo(component3.id)
        assertThat(log[4].dto.state).isEqualTo(ComponentState.State.INITIALIZED)
        assertThat(log[5].dto.id).isEqualTo(component2.id)
        assertThat(log[5].dto.state).isEqualTo(ComponentState.State.STARTED)
        assertThat(log[6].dto.id).isEqualTo(component3.id)
        assertThat(log[6].dto.state).isEqualTo(ComponentState.State.STARTED)
        assertThat(log[7].dto.id).isEqualTo(component2.id)
        assertThat(log[7].dto.state).isEqualTo(ComponentState.State.STOPPED)
        assertThat(log[8].dto.id).isEqualTo(component2.id)
        assertThat(log[8].dto.state).isEqualTo(ComponentState.State.UNLOADED)

        ComponentCache.clear()
        assertThat(ComponentCache).hasSize(0)

        ComponentLogDB.restore("src/test/resources")
        val restoredContexts = ComponentCache.toList()
        assertThat(restoredContexts).hasSize(2)
        assertThat(restoredContexts[0].state).isEqualTo(ComponentState.State.STOPPED)
        assertThat(restoredContexts[1].state).isEqualTo(ComponentState.State.STARTED)
    }


    @Test
    fun `save component data`() {
        val context = testJarUrl.temporaryIsolatedComponent()

        ComponentLogDB.addStateChangeEntry(context)

        assertThat(ComponentLogDB.filter { it.dto.id == context.id }).hasSize(1)
        assertThat(ComponentLogDB.first { it.dto.id == context.id }.dto.state).isEqualTo(context.state)
    }

    @Test
    fun `log component state change`() {
        val context = testJarUrl.temporaryIsolatedComponent()
        val db = ComponentLogDB

        db.addStateChangeEntry(context)

        context.start().get()
        db.addStateChangeEntry(context)

        context.stop().get()
        db.addStateChangeEntry(context)

        context.unload()
        db.addStateChangeEntry(context)

        val stateLogs = db
                .filter { it.dto.id == context.id }
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