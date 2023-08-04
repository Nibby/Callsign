package codes.nibby.callsign.api

class TimedEvent internal constructor(name: String, val startTimeNs: Long?) : Event(name) {

    internal var endTimeNs: Long? = null

}