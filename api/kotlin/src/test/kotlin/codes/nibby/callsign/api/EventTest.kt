package codes.nibby.callsign.api

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EventTest {

    @Test
    fun testConstructor_nameIsBlank_fails() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            TestEvent("  ")
        }
    }

    @Test
    fun testConstructor_nameContainsLeadingWhitespace_fails() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            TestEvent(" name")
        }
    }

    @Test
    fun testConstructor_nameContainsTrailingWhitespace_fails() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            TestEvent("name ")
        }
    }

    @Test
    fun testConstructor_nameOver1024Characters_fails() {
        val longName = StringBuilder()

        for (i in 1..1025) {
            longName.append("a")
        }

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            TestEvent(longName.toString())
        }
    }

    @Test
    fun testConstructor_nameContainsNoLeadingOrTrailingWhitespace_succeeds() {
        val name = "normal name";

        Assertions.assertDoesNotThrow {
            TestEvent(name)
        }

        val event = TestEvent("normal name")

        Assertions.assertEquals(name, event.name)
    }

    @Test
    fun testConstructor_notSavedByDefault() {
        val event = TestEvent("test event")

        Assertions.assertFalse(event.saved)
    }

    @Test
    fun testPutAttribute_nameContainsLeadingWhitespace_fails() {
        val event = TestEvent("test event")

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            event.putAttribute(" name", "")
        }
    }

    @Test
    fun testPutAttribute_nameContainsTrailingWhitespace_fails() {
        val event = TestEvent("test event")

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            event.putAttribute("name ", "")
        }
    }

    @Test
    fun testPutAttribute_nameContainsLeadingAndTrailingWhitespace_fails() {
        val event = TestEvent("test event")

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            event.putAttribute(" name ", "")
        }
    }

    @Test
    fun testPutAttribute_nameIsBlank_fails() {
        val event = TestEvent("test event")

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            event.putAttribute("  ", "")
        }
    }

    @Test
    fun testPutAttribute_nameHasNoLeadingOrTrailingWhitespace_success() {
        val event = TestEvent("test event")

        Assertions.assertDoesNotThrow {
            event.putAttribute("some name", "")
        }
    }

    @Test
    fun testPutAttribute_nameIsEmptyString_fails() {
        val event = TestEvent("test event")

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            event.putAttribute("", "")
        }
    }

    @Test
    fun testPutAttribute_nameExceeds128Characters_fails() {
        val longName = StringBuilder()

        for (i in 1..129) {
            longName.append("a")
        }

        val event = TestEvent("event")

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            event.putAttribute(longName.toString(), "")
        }
    }

    @Test
    fun testPutAttribute_nameUnder128Characters_succeeds() {
        for (nameLength in 1..128) {
            val longName = StringBuilder()
            for (i in 1 .. nameLength) {
                longName.append("a")
            }

            val event = TestEvent("event")

            Assertions.assertDoesNotThrow {
                event.putAttribute(longName.toString(), "")
            }
        }
    }

    @Test

    fun testPutAttribute_eventIsSaved_fails() {
        val event = TestEvent("event")
        event.saved = true

        Assertions.assertThrows(IllegalStateException::class.java) {
            event.putAttribute("name", "")
        }
    }

    @Test
    fun testGetAttribute_nameIsNotPut_returnsNull() {
        val event = TestEvent("event")

        val value: String? = event.getAttribute("something")

        Assertions.assertNull(value)
    }

    @Test
    fun testGetAttribute_nameWasPut_returnsPutValue() {
        val event = TestEvent("event")

        val name = "something"
        val value = "some value"

        event.putAttribute(name, value)
        val retrievedValue: String? = event.getAttribute(name)

        Assertions.assertEquals(value, retrievedValue)
    }

    private class TestEvent(name: String) : Event(name) {

    }
}