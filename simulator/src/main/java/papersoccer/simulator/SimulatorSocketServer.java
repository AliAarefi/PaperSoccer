package papersoccer.simulator;

import papersoccer.common.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SimulatorSocketServer implements Runnable {
	private Logger log = new Logger("SimulatorSocketServer");
	private ServerSocket server;
	int port;
	private Map<UUID, Agent> agents;
	private Map<Socket, SocketAgent> agentsBySocket;

	SimulatorSocketServer(int port, Map<UUID, Agent> agents) throws IOException {
		server = new ServerSocket(port);
		this.port = port;
		this.agents = agents;
		agentsBySocket = new ConcurrentHashMap<>();
	}

	@Override
	public void run() {
		while (true) {
			try {
				Socket socket = server.accept();
				new Thread(() -> {  // TODO Use thread pool
					UUID id = UUID.randomUUID();
					log.d(0, String.format("A new client has been connected. (UUID: %s)", id.toString()));
					try {
						Agent agent = new SocketAgent(socket, id);
						agents.put(id, agent);
						agentsBySocket.put(socket, (SocketAgent) agent);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}).start();
			} catch (IOException ignored) {
			}
		}
	}

	void start() {
		new Thread(this).start();
		log.d(0, String.format("Server is running on port %d", port));
	}

	void stop() {
		log.d(0, "SocketServer is shutting down...");
		for (SocketAgent a : agentsBySocket.values()) {
			try {
				a.socket.close();
			} catch (IOException ignored) {
			}
		}
		try {
			server.close();
		} catch (IOException ignored) {
		}
		try {
			Thread.currentThread().join(0);
		} catch (InterruptedException ignored) {
		}
	}
}
