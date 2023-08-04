package codes.nibby.callsign.api

import kotlinx.serialization.Serializable

@Serializable
internal data class AttributeData(val map: MutableMap<String, String>)