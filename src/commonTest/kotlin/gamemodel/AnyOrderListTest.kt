package gamemodel

import org.junit.Test
import kotlin.test.*

class AnyOrderListTest {

    @Test
    fun `when the lists truly equal, they are considered equal`() {
        assertEquals(anyOrderList(1, 2, 3), listOf(1, 2, 3))
    }

    @Test
    fun `when the lists contains element in different orders, they are considered equal`() {
        assertEquals(anyOrderList(1, 2, 3), listOf(3, 2, 1))
    }

    @Test
    fun `when the lists contains different elements, they are not considered equal`() {
        assertNotEquals(anyOrderList(1, 2, 3, 3), listOf(1, 2, 3))
        assertNotEquals(anyOrderList(1, 2, 3), listOf(1, 2, 3, 3))
    }

    @Test
    fun `it works for properties as well`() {
        data class Container(val list: List<Int>)

        assertEquals(
            Container(anyOrderList(3, 2, 1)),
            Container(listOf(1, 2, 3)),
        )
    }
}
