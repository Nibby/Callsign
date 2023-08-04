package codes.nibby.callsign.api

abstract class Event(val name: String) {

    private val attributeData = AttributeData(HashMap())
    internal var saved: Boolean = false

    internal val lock = Object()

    init {
        assertValidName(name, MAX_EVENT_NAME_LENGTH);
    }

    fun putAttribute(name: String, attribute: String) {
        synchronized(lock) {
            if (saved) {
                throw IllegalStateException("Attempting to modify event after it has been saved: $name")
            }
        }

        assertValidName(name, MAX_ATTRIBUTE_NAME_LENGTH);

        attributeData.map[name] = attribute
    }

    fun getAttribute(name: String) : String? {
        return attributeData.map[name]
    }

    internal fun getAttributeData(): AttributeData {
        return attributeData
    }

    private fun assertValidName(name: String, maxLength: Int) {
        if (name.isBlank() || name.startsWith(" ") || name.endsWith(" ")) {
            throw IllegalArgumentException("Name must not contain leading or trailing whitespace, or be blank")
        }

        if (name.length > maxLength) {
            throw IllegalArgumentException("Name exceeds maximum length of $maxLength")
        }
    }

    companion object {
        val MAX_EVENT_NAME_LENGTH = 1024
        val MAX_ATTRIBUTE_NAME_LENGTH = 128
    }
}