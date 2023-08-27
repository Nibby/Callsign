package codes.nibby.callsign.api

import kotlinx.serialization.Serializable

// TODO: Version serialized data?

@Serializable
internal data class AttributeData(val map: MutableMap<String, String>) {

    internal fun getUserDefinedValue(name: String): String? {
        if (name.startsWith(Event.SPECIAL_ATTRIBUTE_NAME_PREFIX)) {
            return null;
        }

        return map[name]
    }

    internal fun get(name: String): String? {
        return map[name]
    }

    internal fun getUserDefinedNames(): Set<String> {
        return map.filter { entry -> !entry.key.startsWith(Event.SPECIAL_NAME_ATTRIBUTE) }.keys
    }
}