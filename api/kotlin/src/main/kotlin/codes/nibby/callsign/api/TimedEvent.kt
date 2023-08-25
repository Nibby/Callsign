package codes.nibby.callsign.api

class TimedEvent internal constructor(name: String, val startTimeNs: Long?) : Event(TYPE, name) {

    companion object {
        const val TYPE = "t"
    }

    var endTimeNs: Long? = null

}