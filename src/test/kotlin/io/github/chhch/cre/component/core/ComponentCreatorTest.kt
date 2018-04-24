package io.github.chhch.cre.component.core

import org.assertj.core.api.Assertions.*
import org.junit.Test
import java.io.FileNotFoundException
import kotlin.reflect.full.IllegalCallableAccessException

class ComponentCreatorTest{

    private fun resource(path: String) = javaClass.getResource(path)!!

    @Test
    fun `inject component`() {
        val componentInitialization = ComponentCreator.newInstance("id", "name", resource("/componentComposition-1.0-SNAPSHOT-all.jar"))
        assertThat(componentInitialization).isNotNull()
    }

    @Test
    fun `initialize component`() {
        assertThatCode { ComponentCreator.newInstance("id", "name", resource("/helloComponent-1.0-SNAPSHOT-all.jar")) }.doesNotThrowAnyException()
    }

    @Test
    fun `try initialize component with abstract main class`() {
        assertThatThrownBy {
            ComponentCreator.newInstance("id", "name", resource("/helloComponent-1.0-SNAPSHOT-all-abstract-main-class.jar"))
        }.isInstanceOf(InstantiationException::class.java)
    }

    @Test
    fun `try initialize component which constructor throws a exception`() {
        assertThatThrownBy {
            ComponentCreator.newInstance("id", "name", resource("/helloComponent-1.0-SNAPSHOT-all-constructor-throws-exception.jar"))
        }.isInstanceOf(Exception::class.java)
    }

    @Test
    fun `try initialize component with no Main-Class attribute`() {
        assertThatThrownBy {
            ComponentCreator.newInstance("id", "name", resource("/helloComponent-1.0-SNAPSHOT-all-no-main-class-attribute.jar"))
        }.isInstanceOf(ClassNotFoundException::class.java).hasMessageContaining("Main-Class")
    }


    @Test
    fun `try initialize component with no manifest`() {
        assertThatThrownBy {
            ComponentCreator.newInstance("id", "name", resource("/helloComponent-1.0-SNAPSHOT-all-no-manifest.jar"))
        }.isInstanceOf(FileNotFoundException::class.java).hasMessageContaining("Manifest")
    }

    @Test
    fun `try initialize component with no start method`() {
        assertThatThrownBy {
            ComponentCreator.newInstance("id", "name", resource("/helloComponent-1.0-SNAPSHOT-all-no-start.jar"))
        }.isInstanceOf(NoSuchMethodException::class.java).hasMessageContaining("Start")
    }

    @Test
    fun `try initialize component with no stop method`() {
        assertThatThrownBy {
            ComponentCreator.newInstance("id", "name", resource("/helloComponent-1.0-SNAPSHOT-all-no-stop.jar"))
        }.isInstanceOf(NoSuchMethodException::class.java).hasMessageContaining("Stop")
    }

    @Test
    fun `try initialize component with private constructor`() {
        assertThatThrownBy {
            ComponentCreator.newInstance("id", "name", resource("/helloComponent-1.0-SNAPSHOT-all-private-constructor.jar"))
        }.isInstanceOf(IllegalCallableAccessException::class.java)
    }

    @Test
    fun `try initialize component with private main class`() {
        assertThatThrownBy {
            ComponentCreator.newInstance("id", "name", resource("/helloComponent-1.0-SNAPSHOT-all-private-main-class.jar"))
        }.isInstanceOf(IllegalCallableAccessException::class.java)
    }

    @Test
    fun `try initialize component with private start`() {
        assertThatThrownBy {
            ComponentCreator.newInstance("id", "name", resource("/helloComponent-1.0-SNAPSHOT-all-private-start.jar"))
        }.isInstanceOf(IllegalCallableAccessException::class.java).hasMessageContaining("Start")
    }

    @Test
    fun `try initialize component with private stop`() {
        assertThatThrownBy {
            ComponentCreator.newInstance("id", "name", resource("/helloComponent-1.0-SNAPSHOT-all-private-stop.jar"))
        }.isInstanceOf(IllegalCallableAccessException::class.java).hasMessageContaining("Stop")
    }

    @Test
    fun `try initialize component with wrong main class`() {
        assertThatThrownBy {
            ComponentCreator.newInstance("id", "name", resource("/helloComponent-1.0-SNAPSHOT-all-wrong-main-class.jar"))
        }.isInstanceOf(ClassNotFoundException::class.java)
    }

}