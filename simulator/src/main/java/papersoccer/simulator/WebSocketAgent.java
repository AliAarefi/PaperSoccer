package papersoccer.simulator;

import org.java_websocket.WebSocket;

import java.io.IOException;
import java.util.UUID;

class WebSocketAgent extends Agent<WebSocket> {
	WebSocketAgent(WebSocket s, UUID uuid) throws IOException {
		super(s, uuid);
	}

	@Override
	void send(String s) {
		socket.send(s);
	}
}
