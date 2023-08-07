package codes.nibby.callsign.api

class InstantEvent(name: String, val timeNs: Long) : Event(TYPE, name) {

    companion object {
        val TYPE = "i"
    }

    constructor(name: String) : this(name, System.nanoTime()) {

    }

}