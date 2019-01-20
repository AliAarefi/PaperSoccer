package papersoccer.simulator;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import papersoccer.common.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SimulatorWebSocketServer extends WebSocketServer {
	private Logger log = new Logger("SimulatorWebSocketServer");
	private Map<UUID, Agent> agents;
	private Map<WebSocket, WebSocketAgent> agentsBySocket;

	SimulatorWebSocketServer(int port, Map<UUID, Agent> agents) {
		super(new InetSocketAddress(port));
		this.agents = agents;
		agentsBySocket = new ConcurrentHashMap<>();
	}

	SimulatorWebSocketServer(InetSocketAddress address, Map<UUID, Agent> agents) {
		super(address);
		this.agents = agents;
		agentsBySocket = new ConcurrentHashMap<>();
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		UUID id = UUID.randomUUID();
		log.d(0, String.format("A new agent has been connected. (UUID: %s)", id.toString()));
		try {
			Agent agent = new WebSocketAgent(conn, id);
			agents.put(id, agent);
			agentsBySocket.put(conn, (WebSocketAgent) agent);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		WebSocketAgent dead = agentsBySocket.remove(conn);
		log.d(0, String.format("Agent %s has been disconnected.", dead.id.toString()));
		agents.remove(dead.id);
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		MessageQueue.getInstance().add(new Message(agentsBySocket.get(conn).id, message));
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		log.d(0, ex.getMessage());
	}

	@Override
	public void onStart() {
		log.d(0, String.format("WebSocket server is running on port %d", getPort()));
	}
}
