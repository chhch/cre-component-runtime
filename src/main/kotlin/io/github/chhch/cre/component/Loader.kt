package io.github.chhch.cre.component

import io.github.chhch.cre.component.cache.CacheListener
import io.github.chhch.cre.component.cache.ComponentCache
import io.github.chhch.cre.component.context.ComponentContextBase
import io.github.chhch.cre.component.inject.InjectListener
import io.github.chhch.cre.component.persistence.ComponentLogDB
import io.github.chhch.cre.component.persistence.PersistenceScopeListener
import io.github.chhch.cre.component.persistence.PersistenceStateListener
import java.net.URL
import java.util.jar.JarFile

// TODO: Refactor code (URL -> File, Builder, ...)
// FIXME: All components (isolated, temporary) were restored as default Component
/**
 * Load and instance a component from a [JarFile] and log state changes.
 */
fun URL.loadComponent(): ComponentContext =
        ComponentContextBase(
                jar = this,
                stateListener = listOf(CacheListener(), PersistenceStateListener()),
                scopeListener = listOf(InjectListener(), PersistenceScopeListener())
        ).also {
            ComponentLogDB.addInitializeLogEntry(it)
            ComponentCache.add(it)
        }

/**
 * Load and instance a component from a [JarFile] with cache for event and injection support.
 */
fun URL.temporaryComponent(): ComponentContext =
        ComponentContextBase(
                jar = this,
                stateListener = listOf(CacheListener()),
                scopeListener = listOf(InjectListener())
        ).also {
            ComponentCache.add(it)
        }

/**
 * Load and instance a component from a [JarFile] and log state changes.
 */
fun URL.isolatedComponent(): ComponentContext =
        ComponentContextBase(
                jar = this,
                stateListener = listOf(PersistenceStateListener()),
                scopeListener = listOf(PersistenceScopeListener())
        ).also { ComponentLogDB.addInitializeLogEntry(it) }

/**
 * Load and instance a component from a [JarFile].
 *
 * @return[ComponentContextBase] Wrapper for the loaded component instance.
 */
fun URL.temporaryIsolatedComponent(): ComponentContext =
        ComponentContextBase(this)