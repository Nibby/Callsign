package codes.nibby.callsign.api

import kotlinx.serialization.Serializable
import java.util.*

// TODO: Version serialized data?

@Serializable
internal data class AttributeData(val map: MutableMap<String, String>) {

    internal fun get(name: String): String? {
        return map[name]
    }

    internal fun getUserDefinedNames(): Set<String> {
        return map.filter { entry -> !entry.key.startsWith(Event.SPECIAL_NAME_ATTRIBUTE) }.keys
    }


    fun getAllDefinedNames(): Set<String> {
        return map.keys
    }

    override fun equals(other: Any?): Boolean {
        if ((other !is AttributeData)) {
            return false
        }

        if (map.size != other.map.size) {
            return false
        }

        for (key in map.keys) {
            val thisValue = map[key]
            val otherValue = other.map[key]

            if (!Objects.equals(thisValue, otherValue)) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        return map.hashCode()
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("{\n")

        for (key in map.keys) {
            builder.append("    ").append(key).append(": ").append(map[key]).append("\n")
        }

        builder.append("\n}")

        return builder.toString()
    }
}