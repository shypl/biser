package org.shypl.biser.csi.client {
	import flash.utils.ByteArray;
	import flash.utils.IDataInput;

	import org.shypl.biser.csi.CsiException;
	import org.shypl.biser.csi.Protocol;
	import org.shypl.asak.timeline.GlobalTimeline;
	import org.shypl.asak.timeline.TimelineTask;

	public class ConnectionStrategyMessaging extends ConnectionStrategy {

		private static const STATE_FLAG:int = 0;
		private static const STATE_MESSAGE_SIZE:int = 1;
		private static const STATE_MESSAGE_BODY:int = 2;

		private var _pinged:Boolean = true;
		private var _state:int = STATE_FLAG;
		private var _messageEven:Boolean;
		private var _messageSize:int;
		private var _buffer:ByteArray = new ByteArray();
		private var _pingTask:TimelineTask;

		public function ConnectionStrategyMessaging() {
		}

		override public function handleData(data:IDataInput):void {
			switch (_state) {
				case STATE_FLAG:
					readFlag(data);
					break;
				case STATE_MESSAGE_SIZE:
					readMessageSize(data);
					break;
				case STATE_MESSAGE_BODY:
					readMessageBody(data);
					break;
			}
		}

		override public function handleClose():void {
			getLogger().warn("Channel broken");
			processBroken();
		}

		override internal function destroy():void {
			super.destroy();
			if (_pingTask) {
				_pingTask.cancel();
				_pingTask = null;
			}
			_buffer = null;
		}

		override internal function init(connection:Connection, channelHandler:ChannelHandlerNormal):void {
			super.init(connection, channelHandler);
			_pingTask = GlobalTimeline.scheduleRepeatable(30 * 1000, ping);
		}

		private function readFlag(data:IDataInput):void {
			var flag:int = data.readByte();
			switch (flag) {
				case Protocol.PING:
					getLogger().trace("Pong");
					_pinged = true;
					break;
				case Protocol.MESSAGE_ODD:
					setStateMessageSize(false);
					break;
				case Protocol.MESSAGE_EVEN:
					setStateMessageSize(true);
					break;
				case Protocol.MESSAGE_ODD_RECEIVED:
					_connection.completeMessageSend(false);
					break;
				case Protocol.MESSAGE_EVEN_RECEIVED:
					_connection.completeMessageSend(true);
					break;
				default:
					closeByFlag(flag);
					break;
			}
		}

		private function readMessageSize(data:IDataInput):void {
			var i:uint = _buffer.bytesAvailable;
			data.readBytes(_buffer, i, Math.min(4 - i, data.bytesAvailable));
			if (_buffer.bytesAvailable == 4) {
				_state = STATE_MESSAGE_BODY;
				_messageSize = _buffer.readInt();
				_buffer.clear();
			}
		}

		private function readMessageBody(data:IDataInput):void {
			var i:uint = _buffer.bytesAvailable;
			data.readBytes(_buffer, i, Math.min(_messageSize - i, data.bytesAvailable));
			if (_buffer.bytesAvailable == _messageSize) {
				_state = STATE_FLAG;

				var d:ByteArray = new ByteArray();
				_buffer.readBytes(d);
				_buffer.clear();
				_connection.receiveMessage(_messageEven, d);
			}
		}

		private function closeByFlag(flag:int):void {
			var reason:ConnectionCloseReason = ConnectionCloseReason.defineByFlag(flag);
			_connection.doClose(reason);

			if (reason == ConnectionCloseReason.UNDEFINED) {
				throw new CsiException("Invalid flag " + flag + "(" + String.fromCharCode(flag) + ")");
			}
		}

		private function setStateMessageSize(even:Boolean):void {
			_state = STATE_MESSAGE_SIZE;
			_messageEven = even;
		}

		private function processBroken():void {
			_connection.processChannelBroken();
		}

		private function ping():void {
			if (isActive()) {
				if (_pinged) {
					_pinged = false;
					getLogger().trace("Ping");
					_connection.sendByte(Protocol.PING);
				}
				else {
					getLogger().warn("Server is not responding to ping, close channel");
					processBroken();
				}
			}
		}
	}
}
