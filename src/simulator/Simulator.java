package simulator;

import common.Logger;
import common.Watchable;
import common.ServerMessage;
import common.ClientMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class Simulator {
	private int port;
	private Logger log = new Logger("Simulator");
	private ServerSocket server;
	private Environment environment;
	private ConcurrentHashMap<UUID, Agent> agents;
	private UUID players[];
	private Watchable<Boolean> gameReady;
	private boolean simulating = true;
	private Thread messageHandler;

	Simulator(int n) {
		agents = new ConcurrentHashMap<>();
		players = new UUID[n];

		gameReady = new Watchable<>(Boolean.FALSE);

		while (true) {
			try {
				port = (new Random().nextInt() % 60000) + 5000;
				server = new ServerSocket(port);
				break;
			} catch (IOException ignored) {
			}
		}

		log.d(0, String.format("Server is running on port %d", port));

		messageHandler = new Thread(() -> {
			try {
				while (simulating) {
					Message m = MessageQueue.getInstance().take();
					log.d(0, String.format("New message received from agent %s", agents.get(m.id).username));
					handle(m);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		messageHandler.start();

		while (simulating) {
			try {
				Socket socket = server.accept();
				new Thread(() -> {  // TODO Use thread pool
					try {
						UUID id = UUID.randomUUID();
						log.d(0, String.format("A new client has been connected. (%s)", id.toString()));
						Agent agent = new Agent(socket, id);
						agents.put(id, agent);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}).start();
			} catch (IOException ignored) {
			}
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			log.d(0, "Shutting down the simulator...");
			simulating = false;
			try {
				messageHandler.join();
			} catch (InterruptedException ignored) {
			}
		}));
	}

	private void checkGameReady() {
		for (UUID id : players)
			if (id == null) return;
		gameReady.setValue(Boolean.TRUE);
		log.d(0, "Game is ready!");
	}

	void setEnvironment(Environment e) {
		environment = e;
	}

	private void start() {
		environment.clear();
	}

	void startWhenReady() {
		gameReady.setWatcher((obj, oldValue, newValue) -> {
			if (newValue == Boolean.TRUE) start();
		});
	}

	private void handle(Message m) {
		Agent agent = agents.get(m.id);
		String[] message = m.message.split(" ");
		switch (message[0]) {
			case ClientMessage.agent_authentication:
				if (message.length == 2)
					agent.username = message[1];
				break;
			case ClientMessage.agent_type:
				if (message.length == 3) {
					if (message[1].equals("player")) {
						int side = Integer.parseInt(message[2]);
						if (players[side] == null) {
							agent.setPlayer(true, side);
							log.d(0, String.format("Player %d is set.", agent.side));
							checkGameReady();
						} else
							agent.send(ServerMessage.error_players_full);
					}
				}
				break;
		}
	}

	public static void main(String[] args) {
		Simulator a = new Simulator(2);  // Paper Soccer specific
		a.setEnvironment(new Environment(10, 8));  // Paper Soccer specific
		a.startWhenReady();
	}
}
