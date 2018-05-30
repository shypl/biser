package org.shypl.biser.csi.client

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.shypl.biser.io.ArrayBufferInputData
import org.w3c.dom.ARRAYBUFFER
import org.w3c.dom.BinaryType
import org.w3c.dom.CloseEvent
import org.w3c.dom.ErrorEvent
import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import ru.capjack.ktjs.common.invokeDelayed

class WebSocketChannel(
	address: String,
	private val acceptor: ChannelAcceptor

) : Channel, EventListener {
	private val socket: WebSocket
	private var opened = false
	private lateinit var handler: ChannelHandler
	
	init {
		try {
			socket = WebSocket(address)
			socket.binaryType = BinaryType.ARRAYBUFFER
			socket.addEventListener("open", this)
			socket.addEventListener("close", this)
			socket.addEventListener("message", this)
			socket.addEventListener("error", this)
		} catch (e: Throwable) {
			acceptor.failOpenChannel(e)
			throw RuntimeException(e)
		}
	}
	
	override fun handleEvent(event: Event) {
		when (event.type) {
			"open"    -> onOpen()
			"close"   -> onClose(event.unsafeCast<CloseEvent>())
			"message" -> onMessage(event.unsafeCast<MessageEvent>())
			"error"   -> onError(event.unsafeCast<ErrorEvent>())
		}
	}
	
	override fun writeByte(byte: Byte) {
		if (opened) {
			socket.send(Int8Array(arrayOf(byte)))
		} else {
			throw IllegalStateException()
		}
	}
	
	override fun writeBytes(bytes: ByteArray) {
		if (opened) {
			socket.send(Int8Array(bytes.toTypedArray()))
		} else {
			throw IllegalStateException()
		}
	}
	
	override fun close() {
		if (opened) {
			opened = false
			invokeDelayed(handler::handleChannelClose)
		}
		free()
	}
	
	private fun onOpen() {
		opened = true
		handler = acceptor.acceptChannel(this)
	}
	
	private fun onError(event: ErrorEvent) {
		if (opened) {
			opened = false
			handler.handleChannelError(event)
			close()
		} else {
			acceptor.failOpenChannel(event)
			free()
		}
	}
	
	private fun free() {
		socket.removeEventListener("open", this)
		socket.removeEventListener("close", this)
		socket.removeEventListener("message", this)
		socket.removeEventListener("error", this)
		
		try {
			socket.close()
		} catch (e: Throwable) {
		}
	}
	
	private fun onClose(event: CloseEvent) {
		console.warn("WebSocketChannel.onClose", event)
		close()
	}
	
	private fun onMessage(event: MessageEvent) {
		handler.handleChannelData(ArrayBufferInputData(event.data.unsafeCast<ArrayBuffer>()))
	}
}
