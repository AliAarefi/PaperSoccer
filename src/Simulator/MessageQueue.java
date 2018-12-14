package simulator;

import java.util.LinkedList;

public class MessageQueue extends LinkedList<Message> {
	private static MessageQueue instance;

	private MessageQueue() {}

	static MessageQueue getInstance() {
		if (instance == null)
			instance = new MessageQueue();
		return instance;
	}
}
