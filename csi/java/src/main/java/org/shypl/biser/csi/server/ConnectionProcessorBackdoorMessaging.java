package org.shypl.biser.csi.server;

import org.shypl.biser.csi.ByteBuffer;
import org.shypl.biser.csi.ConnectionCloseReason;
import org.shypl.biser.csi.ProtocolException;

import java.util.ArrayList;
import java.util.List;

public class ConnectionProcessorBackdoorMessaging extends ConnectionProcessor {
	
	private ConnectionStringLineBuffer lineBuffer = new ConnectionStringLineBuffer();
	private BackdoorConnectionImpl bdConnection;
	
	@Override
	public void init(Connection connection) {
		super.init(connection);
		bdConnection = new BackdoorConnectionImpl();
		connection.cancelCheckActivityTimeout();
	}
	
	@Override
	public void processData() throws ProtocolException {
		if (lineBuffer.fill(connection)) {
			parse(lineBuffer.read());
		}
	}
	
	private void parse(String line) {
		String command = null;
		List<String> arguments = new ArrayList<>();
		int s = 0;
		int i = 0;
		boolean a = true;
		for (; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == ' ') {
				if (a) {
					if (command == null) {
						command = line.substring(s, i);
					}
					else {
						arguments.add(line.substring(s, i));
					}
				}
				s = i + 1;
				a = false;
			}
			else {
				a = true;
			}
		}
		if (s < i) {
			if (command == null) {
				command = line.substring(s, i);
			}
			else {
				arguments.add(line.substring(s, i));
			}
		}
		
		processCommand(command, arguments.toArray(new String[arguments.size()]));
	}
	
	private void processCommand(String command, String[] arguments) {
		connection.getServer().processBackdoorCommand(bdConnection, command, arguments);
	}
	
	private class BackdoorConnectionImpl implements BackdoorConnection {
		private final ByteBuffer buffer = new ByteBuffer();
		
		
		@Override
		public void writeInt(int i) {
			buffer.writeInt(i);
			connection.send(buffer.readBytes());
			buffer.clear();
		}
		
		@Override
		public void writeLong(long l) {
			buffer.writeLong(l);
			connection.send(buffer.readBytes());
			buffer.clear();
		}
		
		@Override
		public void writeString(String s) {
			buffer.writeString(s);
			connection.send(buffer.readBytes());
			buffer.clear();
		}
		
		@Override
		public void writeLine(String s) {
			buffer.writeString(s);
			buffer.writeByte((byte)'\n');
			connection.send(buffer.readBytes());
			buffer.clear();
		}
		
		@Override
		public void close() {
			connection.close(ConnectionCloseReason.NONE);
		}
	}
}
