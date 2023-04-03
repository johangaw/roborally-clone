package gamemodel

import com.soywiz.korio.file.std.*
import kotlin.test.*
import com.soywiz.korio.async.suspendTest
import kotlinx.serialization.json.*
import kotlinx.serialization.decodeFromString
import kotlin.test.Test

class CourseLibraryTest {

    @Test
    fun `ensure deserialization is possible for all courses`() = suspendTest {
        val json = Json {
            allowStructuredMapKeys = true
        }
        resourcesVfs["courses"].listSimple().forEach {
            val data = it.readString()
            assertNotFails("Unable to parse ${it.path}") { json.decodeFromString<Course>(data) }
        }
    }
}

fun assertNotFails(message: String = "", block: () -> Unit) {
    try {
        block()
    } catch(err: Throwable) {
        throw AssertionError("Action failed: $message", err)
    }
}

