package Simulator;

import common.Logger;
import common.Strings;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

class Simulator {
	private Logger log = new Logger("Simulator");
	private ServerSocket server;
	private Environment environment;
	private HashMap<UUID, Agent> agents;
	private UUID players[];
	private boolean gameReady = false;

	Simulator(int n) {
		agents = new HashMap<>();
		players = new UUID[n];

		while (true) {
			try {
				server = new ServerSocket((new Random().nextInt() % 60000) + 5000);
				break;
			} catch (IOException ignored) {
			}
		}

		while (!gameReady) {
			try {
				Socket socket = server.accept();
				new Thread(() -> {  // TODO Use thread pool
					try {
						UUID id = UUID.randomUUID();
						log.d(1, String.format("A new client has been connected. (%s)", id.toString()));
						Agent agent = new Agent(socket, id);
						agents.put(id, agent);

						agent.initialize();
						if (agent.isPlayer()) {
							if (players[agent.side] == null) {
								players[agent.side] = id;
								log.d(1, String.format("Player %d is set.", agent.side));
								checkGameReady();
							} else
								agent.send(Strings.error_players_full);  // FIXME Player is already set in the agent object
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}).start();
			} catch (IOException ignored) {
			}
		}
	}

	private void checkGameReady() {
		for (UUID id : players)
			if (id == null) return;
		gameReady = true;
		log.d(1, "Game is ready!");
	}

	void setEnvironment(Environment e) {
		environment = e;
	}

	private void start() {

	}

	void startWhenReady() {

	}

	public static void main(String[] args) {
		Simulator a = new Simulator(2);  // Paper Soccer specific
		a.setEnvironment(new Environment(10, 8));  // Paper Soccer specific
		a.startWhenReady();
	}
}
