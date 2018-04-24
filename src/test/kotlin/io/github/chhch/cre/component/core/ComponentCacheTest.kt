package io.github.chhch.cre.component.core


import io.github.chhch.cre.component.ComponentState
import io.github.chhch.cre.component.cache.ComponentCache
import io.github.chhch.cre.component.temporaryComponent
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.BeforeClass
import org.junit.Test
import java.net.URL

class ComponentCacheTest {

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
        }
    }

    @After
    fun cleanupTest() {
        ComponentCache.clear()
    }

    @Test
    fun `cache composite component`() {
        val (toInjectUrl, compositionUrl) = urls

        val toInjectContext = toInjectUrl.temporaryComponent()
        val compositionContext = compositionUrl.temporaryComponent()

        assertThat(toInjectContext.state).isEqualTo(ComponentState.State.INITIALIZED)
        assertThat(compositionContext.state).isEqualTo(ComponentState.State.INITIALIZED)

        assertThat(ComponentCache.getContextWithDependedLoader(toInjectContext)).contains(compositionContext)
        assertThat(ComponentCache.getContextWithDependedLoader(compositionContext)).isEmpty()
    }

    @Test
    fun `unload injected component`() {
        val (toInjectUrl, compositionUrl) = urls

        val toInjectContext = toInjectUrl.temporaryComponent()
        val compositionContext = compositionUrl.temporaryComponent()

        assertThat(toInjectContext.state).isEqualTo(ComponentState.State.INITIALIZED)
        assertThat(compositionContext.state).isEqualTo(ComponentState.State.INITIALIZED)

        toInjectContext.unload()

        assertThat(toInjectContext.state).isEqualTo(ComponentState.State.UNLOADED)
        assertThat(compositionContext.state).isEqualTo(ComponentState.State.UNLOADED)
        assertThat(ComponentCache).isEmpty()
    }

    @Test
    fun `unload composited component`() {
        val (toInjectUrl, compositionUrl) = urls

        val toInjectContext = toInjectUrl.temporaryComponent()
        val compositionContext = compositionUrl.temporaryComponent()

        assertThat(toInjectContext.state).isEqualTo(ComponentState.State.INITIALIZED)
        assertThat(compositionContext.state).isEqualTo(ComponentState.State.INITIALIZED)

        compositionContext.unload()

        assertThat(toInjectContext.state).isEqualTo(ComponentState.State.INITIALIZED)
        assertThat(compositionContext.state).isEqualTo(ComponentState.State.UNLOADED)
        assertThat(ComponentCache).hasSize(1)
    }
}

