package simulator;

import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueue extends LinkedBlockingQueue<Message> {
	private static MessageQueue instance;

	private MessageQueue() {}

	static MessageQueue getInstance() {
		if (instance == null)
			instance = new MessageQueue();
		return instance;
	}
}
