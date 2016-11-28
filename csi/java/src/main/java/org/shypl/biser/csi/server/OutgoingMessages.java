package org.shypl.biser.csi.server;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

public class OutgoingMessages {
	
	private int                    idCounter = 0;
	private Deque<OutgoingMessage> list      = new LinkedList<>();
	private int lastReleasedId;
	
	public OutgoingMessage create(byte[] data) {
		OutgoingMessage message = new OutgoingMessage(nextId(), data);
		list.addLast(message);
		return message;
	}
	
	public void releaseFirst() {
		lastReleasedId = list.removeFirst().id;
	}
	
	public void releaseTo(int id) {
		while (lastReleasedId != id && !list.isEmpty()) {
			releaseFirst();
		}
	}
	
	public Collection<OutgoingMessage> getQueue() {
		return list;
	}
	
	private int nextId() {
		int id = ++idCounter;
		if (id == 0) {
			id = ++idCounter;
		}
		return id;
	}
	
}
