package org.shypl.biser.csi.client

internal class OutgoingMessages {
	val queue: List<OutgoingMessage>
		get() = list
	
	private val list = mutableListOf<OutgoingMessage>()
	private var idCounter = 0
	private var lastReleasedId = 0
	
	fun create(data: ByteArray): OutgoingMessage {
		val message: OutgoingMessage = OutgoingMessage(nextId(), data)
		list.add(message)
		return message
	}
	
	fun releaseTo(id: Int) {
		while (lastReleasedId != id && !list.isEmpty()) {
			val first: OutgoingMessage = list.removeAt(0)
			lastReleasedId = first.id
		}
	}
	
	
	private fun nextId(): Int {
		var id = ++idCounter
		if (id == 0) {
			id = ++idCounter
		}
		return id
	}
}
