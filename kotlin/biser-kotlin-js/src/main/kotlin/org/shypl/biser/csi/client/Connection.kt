package org.shypl.biser.csi.client

import org.shypl.biser.StringUtils
import org.shypl.biser.csi.ConnectionCloseReason
import org.shypl.biser.csi.Protocol
import org.shypl.biser.io.ByteArrayOutputData
import org.shypl.biser.io.InputData
import org.shypl.biser.io.OutputData
import ru.capjack.ktjs.common.invokeDelayed
import ru.capjack.ktjs.common.logging.Logging

internal class Connection(
	val client: Client,
	private var address: String,
	authorizationKey: String
) : ChannelHandler, ChannelAcceptor {
	val logger = Logging.get<Connection>()
	
	var opened: Boolean = true
		private set
	private var alive: Boolean = false
	private var messaging: Boolean = false
	
	private var processor: ConnectionProcessor = ConnectionProcessorAuthorization(authorizationKey)
	private var channel: Channel? = null
	private lateinit var closeReason: ConnectionCloseReason
	
	var sid: ByteArray? = null
		private set
	var activityTimeout: Int = 0
		private set
	var recoveryTimeout: Int = 0
		private set
	
	private val outgoingMessages = OutgoingMessages()
	private val outgoingMessageBuffer = ByteArrayOutputData()
	
	var lastIncomingMessageId: Int = 0
		private set
	
	private var recoverMessageId: Int = 0
	
	init {
		this.processor.init(this)
		openChannel()
	}
	
	fun close(reason: ConnectionCloseReason) {
		if (opened) {
			logger.debug("Close by reason {} (alive: {})", reason, alive)
			
			opened = false
			closeReason = reason
			
			if (alive) {
				if (messaging) {
					channel!!.writeByte(Protocol.CLOSE)
				}
				channel!!.close()
			} else {
				invokeDelayed(::handleChannelClose)
			}
		}
	}
	
	override fun acceptChannel(channel: Channel): ChannelHandler {
		if (opened) {
			logger.debug("Channel opened")
			
			alive = true
			this.channel = channel
			processor.processAccept()
			return this
		}
		
		logger.debug("Channel opened on closed connection")
		channel.close()
		
		throw IllegalStateException()
	}
	
	override fun failOpenChannel(error: Any) {
		logger.debug("Open channel failed ({})", error)
		invokeDelayed { client.processConnectFail(error) }
	}
	
	override fun handleChannelClose() {
		logger.debug("Channel closed (alive: {})", alive)
		
		messaging = false
		alive = false
		channel = null
		if (opened) {
			invokeDelayed(client::processConnectionInterrupted)
			processor.processClose()
		} else {
			processor.destroy()
			invokeDelayed { client.processDisconnected(closeReason) }
			free()
		}
	}
	
	override fun handleChannelData(data: InputData) {
		if (logger.traceEnabled) {
			logger.trace("<< {}", StringUtils.toString(data.toByteArray()))
		}
		
		while (opened && data.isReadable()) {
			processor.processData(data)
		}
	}
	
	override fun handleChannelError(error: Any) {
		logger.warn("Channel error {} ", error)
	}
	
	internal fun setProcessor(processor: ConnectionProcessor) {
		this.processor.destroy()
		this.processor = processor
		this.processor.init(this)
	}
	
	internal fun openChannel() {
		logger.debug("Open channel")
		client.channelProvider.openChannel(address, this)
	}
	
	internal fun interrupt() {
		if (alive) {
			channel?.close()
		} else {
			invokeDelayed(::handleChannelClose)
		}
	}
	
	internal fun beginSession(sid: ByteArray, activityTimeout: Int, recoveryTimeout: Int) {
		if (logger.debugEnabled) {
			logger.debug("Begin session (sid: {}, activityTimeout: {}, recoveryTimeout: {})", StringUtils.toString(sid), activityTimeout, recoveryTimeout)
		}
		
		this.sid = sid
		this.activityTimeout = activityTimeout
		this.recoveryTimeout = recoveryTimeout
		
		invokeDelayed(::beginSessionDelayed)
	}
	
	internal fun recoverSession(sid: ByteArray, messageId: Int) {
		logger.debug("Recover session (sid: {})", sid)
		this.sid = sid
		this.recoverMessageId = messageId
		
		invokeDelayed(::recoverSessionDelayed)
	}
	
	internal fun sendByte(byte: Byte) {
		if (alive) {
			writeByteToChannel(byte)
		} else {
			logger.error("Can't send data on interrupted connection")
		}
	}
	
	internal fun sendBytes(data: ByteArray) {
		if (alive) {
			writeBytesToChannel(data)
		} else {
			logger.error("Can't send data on interrupted connection")
		}
	}
	
	internal fun sendMessage(data: OutputData) {
		if (data.isEmpty()) {
			throw IllegalArgumentException("Outgoing message is empty")
		}
		
		if (opened) {
			val message = outgoingMessages.create(data.toByteArray())
			if (messaging) {
				sendMessage0(message)
			}
		} else {
			logger.warn("Fail send message on closed connection")
		}
	}
	
	internal fun receiveMessage(messageId: Int, message: InputData) {
		lastIncomingMessageId = messageId
		
		if (!message.isReadable()) {
			throw IllegalArgumentException("Received message is empty")
		}
		
		client.api.processIncomingMessage(message)
	}
	
	internal fun processOutgoingMessageReceived(messageId: Int) {
		outgoingMessages.releaseTo(messageId)
	}
	
	internal fun sendPing() {
		outgoingMessageBuffer.writeByte(Protocol.PING)
		writeMessageReceivedFlagsToOutgoingMessageBuffer()
		sendBytes(outgoingMessageBuffer.toByteArray())
		outgoingMessageBuffer.clear()
	}
	
	private fun beginSessionDelayed() {
		messaging = true
		
		sendQueuedMessages()
		
		client.processConnected()
	}
	
	private fun recoverSessionDelayed() {
		messaging = true
		
		outgoingMessages.releaseTo(recoverMessageId)
		sendQueuedMessages()
		
		client.processConnectionEstablished()
	}
	
	private fun sendQueuedMessages() {
		for (message in outgoingMessages.queue) {
			sendMessage0(message)
		}
	}
	
	private fun sendMessage0(message: OutgoingMessage) {
		
		writeMessageReceivedFlagsToOutgoingMessageBuffer()
		
		outgoingMessageBuffer.writeByte(Protocol.MESSAGE)
		outgoingMessageBuffer.writeInt(message.id)
		outgoingMessageBuffer.writeInt(message.data.size)
		outgoingMessageBuffer.writeBytes(message.data)
		
		sendBytes(outgoingMessageBuffer.toByteArray())
		
		outgoingMessageBuffer.clear()
	}
	
	private fun writeMessageReceivedFlagsToOutgoingMessageBuffer() {
		outgoingMessageBuffer.writeByte(Protocol.MESSAGE_RECEIVED)
		outgoingMessageBuffer.writeInt(lastIncomingMessageId)
	}
	
	private fun writeByteToChannel(byte: Byte) {
		if (logger.traceEnabled) {
			logger.trace(">> {}", "[$byte]")
		}
		channel?.writeByte(byte)
	}
	
	private fun writeBytesToChannel(bytes: ByteArray) {
		if (logger.traceEnabled) {
			logger.trace(">> {}", StringUtils.toString(bytes))
		}
		channel?.writeBytes(bytes)
	}
	
	private fun free() {
		channel = null
		sid = null
	}
}