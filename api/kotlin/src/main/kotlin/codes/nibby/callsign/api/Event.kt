package codes.nibby.callsign.api

abstract class Event(val type: String, name: String) {

    private val attributeData = AttributeData(HashMap())
    internal var saved: Boolean = false

    internal val lock = Object()

    init {
        assertValidName(name, MAX_EVENT_NAME_LENGTH - 1)
        attributeData.map[RESERVED_NAME_ATTRIBUTE] = name
    }

    fun getName() : String {
        return getAttribute(RESERVED_NAME_ATTRIBUTE)!!
    }

    fun putAttribute(name: String, value: String) {
        synchronized(lock) {
            if (saved) {
                throw IllegalStateException("Attempting to modify event after it has been saved: $name")
            }
        }

        assertValidName(name, MAX_ATTRIBUTE_NAME_LENGTH);

        attributeData.map[name] = value
    }

    fun getAttribute(name: String) : String? {
        return attributeData.map[name]
    }

    fun getAttributeNames() : Set<String> {
        return attributeData.map.keys
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
        const val MAX_EVENT_NAME_LENGTH = 1024
        const val MAX_ATTRIBUTE_NAME_LENGTH = 128

        private const val RESERVED_ATTRIBUTE_NAME_PREFIX = "$"
        const val RESERVED_NAME_ATTRIBUTE = RESERVED_ATTRIBUTE_NAME_PREFIX + "event_name"
    }
}