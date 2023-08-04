package codes.nibby.callsign.api

class InstantEvent(name: String, val timeNs: Long) : Event(name) {

    constructor(name: String) : this(name, System.nanoTime()) {

    }

}