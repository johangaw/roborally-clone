package gamemodel

import com.soywiz.korio.file.std.*
import com.soywiz.korio.async.suspendTest
import kotlin.test.*

class ModelStorageTest {

    @Test
    fun `ensure deserialization is possible for all courses`() = suspendTest {
        resourcesVfs["courses"].listSimple().forEach {
            val data = it.readString()
            assertNotFails("Unable to parse ${it.path}") { deserializeCourse(data) }
        }
    }

    @Test
    fun `ensure serialisation of the game model works`() = suspendTest {
        val gameModel = setupGame(PreBuildCourse.Course1, 6)
        assertEquals(gameModel, deserializeGameModel(serialize(gameModel)))
    }
}

fun assertNotFails(message: String = "", block: () -> Unit) {
    try {
        block()
    } catch(err: Throwable) {
        throw AssertionError("Action failed: $message", err)
    }
}

