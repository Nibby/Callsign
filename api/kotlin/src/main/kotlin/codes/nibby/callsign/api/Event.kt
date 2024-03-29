@file:Suppress("MemberVisibilityCanBePrivate")

package codes.nibby.callsign.api

import java.util.*
import kotlin.collections.HashMap

/**
 * Represents a notable occurrence during the execution of a program. At its core, an event consists
 * of a set of attributes, the approximate occurrence time in milliseconds, and an internal unique
 * type code to distinguish one type of event from another.
 *
 * Event attributes are any metadata that is associated with the event. This should be used to record
 * any notable information or program state at the time of the event. Attributes are keyed by the
 * attribute name. Two types of attributes exist: user-defined and special attributes. Special
 * attributes are used by this API to store event metadata such as the event name. These are always
 * prefixed with the [SPECIAL_ATTRIBUTE_NAME_PREFIX] character. On the other hand, user-defined
 * attributes are those submitted from the [putAttribute] method. It is not possible to modify
 * reserved attributes.
 *
 * A [TimelineLogger] records events during the lifetime of a program. The logger has a pre-configured
 * [TimelineLogSink] to store the event data into. Once an event has been logged, it is considered
 * "saved". Attempting to modify saved events will result in an exception.
 *
 * @param existingId Unique identifier for this event. If null, a new ID will be generated for this event.
 *                   This value should only be filled if the event is being loaded from a persisted source.
 *                   All new events should have new generated IDs.
 *
 * @param correlationId Optional parameter. If this event is associated with another event, this is the
 *                      [eventId] of the other event it is related to. Otherwise, leave null.
 *
 * @param type Internal type code for this family of event. Must be unique among all families
 * @param name Name of this event, stored as a special attribute accessible from [getAttribute]
 * @param timeMs The approximate time (in milliseconds) this event occurred on
 *
 * @see InstantEvent
 * @see IntervalStartEvent
 * @see IntervalEndEvent
 * @see TimelineLogger
 */
abstract class Event(existingId: UUID?, val correlationId: UUID?, val type: String, name: String, val timeMs: Long) {

    /**
     * A unique identifier for this event. May be referenced by other events [correlationId].
     */
    val id: UUID

    /**
     * The name of this event
     */
    val name: String
        get() = getSpecialAttribute(SPECIAL_NAME_ATTRIBUTE)!!

    internal var published: Boolean = false
    internal val lock = Object()

    private val attributeData = AttributeData(HashMap())

    init {
        if (name.isBlank() || name.startsWith(" ") || name.endsWith(" ")) {
            throw IllegalArgumentException("Name must not contain leading or trailing whitespace, or be blank")
        }

        putSpecialAttribute(SPECIAL_NAME_ATTRIBUTE, name)

        id = existingId ?: UUID.randomUUID()
    }

    /**
     * Stores a user-defined attribute for this event, overriding any previous value under the same
     * name. The name must not begin with [SPECIAL_ATTRIBUTE_NAME_PREFIX] as it is reserved for
     * special attributes used internally by the Callsign API.
     *
     * An attribute may be any information associated with this event.
     *
     * This operation will fail if the event has already been logged by a [TimelineLogger].
     *
     * @param name Name of the attribute
     * @param value Value for this attribute
     *
     * @see getAttribute
     * @see getUserAttributeNames
     */
    fun putAttribute(name: String, value: String) {
        assertNotSaved()
        assertValidName(name, MAX_ATTRIBUTE_NAME_LENGTH, isForSpecialAttribute = false)

        attributeData.map[name] = value
    }

    /**
     * @return The value of a previously stored user attribute, or null if none exists with that name
     *
     * @see putAttribute
     */
    fun getAttribute(name: String) : String? {
        return attributeData.get(name)
    }

    /**
     * Stores an internal attribute for this event, overriding any previous value under the same
     * name. The name must begin with [SPECIAL_ATTRIBUTE_NAME_PREFIX].
     *
     * Special attributes are used internally by the Callsign API to store metadata about the event,
     * such as its event name.
     *
     * @see getSpecialAttribute
     */
    internal fun putSpecialAttribute(name: String, value: String) {
        assertNotSaved()
        assertValidName(name, MAX_ATTRIBUTE_NAME_LENGTH - 1, isForSpecialAttribute = true)

        attributeData.map[name] = value
    }

    /**
     * @return The value of a previously stored special attribute, or null if none exists with that name
     *
     * @see putSpecialAttribute
     */
    internal fun getSpecialAttribute(name: String) : String? {
        return attributeData.get(name)
    }

    /**
     * @return Immutable set of all user-defined attribute names for this event.
     */
    fun getUserAttributeNames() : Set<String> {
        return Collections.unmodifiableSet(attributeData.getUserDefinedNames())
    }

    /**
     * @return Immutable set of all attribute names (including special attributes) for this event.
     */
    fun getAllAttributeNames() : Set<String> {
        return Collections.unmodifiableSet(attributeData.getAllDefinedNames())
    }

    internal fun getAttributeData(): AttributeData {
        return attributeData
    }

    internal fun loadAttributeData(data: AttributeData, includeSpecialAttributes: Boolean) {
        for (entry in data.map.entries) {
            val name = entry.key
            val value = entry.value

            if (name.startsWith(SPECIAL_ATTRIBUTE_NAME_PREFIX)) {
                if (includeSpecialAttributes) {
                    putSpecialAttribute(name, value)
                }
            } else {
                putAttribute(name, value)
            }
        }
    }

    private fun assertNotSaved() {
        synchronized(lock) {
            if (published) {
                throw IllegalStateException("Attempting to modify event after it has been saved: $name")
            }
        }
    }

    private fun assertValidName(name: String, maxLength: Int, isForSpecialAttribute: Boolean) {
        if (name.isBlank() || name.startsWith(" ") || name.endsWith(" ")) {
            throw IllegalArgumentException("Name must not contain leading or trailing whitespace, or be blank")
        }

        if (name.length > maxLength) {
            throw IllegalArgumentException("Name exceeds maximum length of $maxLength")
        }

        if (isForSpecialAttribute && !name.startsWith(SPECIAL_ATTRIBUTE_NAME_PREFIX)) {
            throw IllegalArgumentException("Special attribute names must always begin with $SPECIAL_ATTRIBUTE_NAME_PREFIX")
        }

        if (!isForSpecialAttribute && name.startsWith(SPECIAL_NAME_ATTRIBUTE)) {
            throw IllegalArgumentException("Name cannot begin with $SPECIAL_ATTRIBUTE_NAME_PREFIX")
        }
    }

    override fun toString(): String {
        return "[Event '$name', id='$id']"
    }

    companion object {
        /** Max character length allowed for attribute names */
        const val MAX_ATTRIBUTE_NAME_LENGTH = 128

        internal const val SPECIAL_ATTRIBUTE_NAME_PREFIX = "$"

        const val SPECIAL_NAME_ATTRIBUTE = SPECIAL_ATTRIBUTE_NAME_PREFIX + "event_name"
    }
}