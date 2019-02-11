package papersoccer.simulator;

import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

import java.io.IOException;
import java.util.UUID;

class WebSocketAgent extends Agent<WebSocket> {
	WebSocketAgent(WebSocket s, UUID uuid) throws IOException {
		super(s, uuid);
	}

	@Override
	boolean send(String s) {
		try {
			socket.send(s);
			return true;
		} catch (WebsocketNotConnectedException ignored) {
			return false;
		}
	}
}
