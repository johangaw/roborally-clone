package gamemodel

import com.soywiz.korio.file.std.*
import com.soywiz.korio.async.suspendTest
import kotlinx.coroutines.flow.*
import kotlin.collections.forEach
import kotlin.test.*

class ModelStorageTest {

    @Test
    fun `ensure deserialization is possible for all courses`() = suspendTest {
        resourcesVfs["courses"]
            .listRecursive { it.path.endsWith(".json") }
            .collect {
                val data = it.readString()
                assertNotFails("Unable to parse ${it.path}") { deserializeCourse(data) }
            }
    }

    @Test
    fun `ensure serialisation of the game model works`() = suspendTest {
        val res = setupGame(PreBuildCourse.Course1, 6) as SetupGameResult.Success
        assertEquals(res.gameModel, deserializeGameModel(serialize(res.gameModel)))
    }
}

fun assertNotFails(message: String = "", block: () -> Unit) {
    try {
        block()
    } catch (err: Throwable) {
        throw AssertionError("Action failed: $message", err)
    }
}

