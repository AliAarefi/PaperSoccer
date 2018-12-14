package simulator;

import java.util.UUID;

class Message {
	UUID id;
	String message;

	Message(UUID id, String message) {
		this.id = id;
		this.message = message;
	}
}
