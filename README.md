# cre-component-runtime

Core module for managing components. Publish locally with `gradlew publishToMavenLocal`. 

## Load Component

```kotlin
val componentUrl = javaClass.getResource("/helloComponent.jar")!!
val component = componentUrl.loadComponent()
component.start()
```

For more information, see [Loader](src/main/kotlin/io/github/chhch/cre/component/Loader.kt).

## Work with Cache

```kotlin
// Get cached components from current session
val components = CRECache.getAllComponents()

// Restore components from previous sessions
CRECache.restore()  
```

For more information, see [Cache](src/main/kotlin/io/github/chhch/cre/component/Cache.kt).

## Read Log Messages from Components
```kotlin
CRELog.subscribe(listener)

class Listener : LogListener {
        override fun invoke(msg: ComponentLogMessage) = 
        println(logMessage)
}
```

For more information, see [Log](src/main/kotlin/io/github/chhch/cre/component/Log.kt).