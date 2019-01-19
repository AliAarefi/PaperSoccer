package papersoccer.simulator;

import papersoccer.common.ClientMessage;
import papersoccer.common.Logger;
import papersoccer.common.ServerMessage;
import papersoccer.common.Watchable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class Simulator {
	private Logger log = new Logger("Simulator");
	private int port;
	private ServerSocket server;
	private Environment environment;
	private ConcurrentHashMap<UUID, Agent> agents;
	private UUID players[];
	private Watchable<Boolean> gameReady;
	private boolean simulating = true;
	private Thread messageHandler;
	private String turn;

	Simulator(int n) {
		agents = new ConcurrentHashMap<>();
		players = new UUID[n];

		gameReady = new Watchable<>(Boolean.FALSE);

		while (true) {
			try {
				port = new Random().nextInt(60000) + 5000;
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
						log.d(0, String.format("A new client has been connected. (UUID: %s)", id.toString()));
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
				server.close();
			} catch (IOException ignored) {
			}
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
				if (message.length == 2) {
					// TODO check username uniqueness
					agent.username = message[1];
					agent.send(ServerMessage.authentication_approved);
					agent.send(ServerMessage.world_broadcast);
					agent.send(environment.convertToString());
					agent.send(ServerMessage.ball_position_broadcast);
					agent.send(Integer.toString(environment.getBallPosition()));
				} else agent.send(ServerMessage.authentication_failed);
				break;

			case ClientMessage.join_request:
				if (message.length == 2) {
					int side = Integer.parseInt(message[1]);
					if (players[side] == null) {
						agent.setPlayer(true, side);
						players[side] = agent.id;
						log.d(0, String.format("Player %d is set.", agent.side));
						checkGameReady();
					} else {
						if (players[players.length - 1 - side] == null)
							agent.send(ServerMessage.join_failed);
						else
							agent.send(ServerMessage.error_players_full);
					}
				} else agent.send(ServerMessage.join_failed);
				break;

			case ClientMessage.leave_request:
				if (true) {
					agent.send(ServerMessage.leave_accepted);
				} else {
					agent.send(ServerMessage.leave_failed);
				}
				break;

			case ClientMessage.action_request:
				if (message.length == 3) {
					if (environment.doAction(Integer.parseInt(message[1]), Integer.parseInt(message[2]))) {
						agent.send(ServerMessage.action_accepted);
						// broadcast world & ballPosition & turn
						turn = whoseTurnIsIt(environment.getTurn());
						for (Agent ag : agents.values()) {
							ag.send(ServerMessage.world_broadcast);
							ag.send(environment.convertToString());
							ag.send(ServerMessage.ball_position_broadcast);
							ag.send(Integer.toString(environment.getBallPosition()));
							ag.send(ServerMessage.turn_broadcast);
							ag.send(turn);
						}
					} else {
						agent.send(ServerMessage.action_failed);
					}
				} else {
					agent.send(ServerMessage.action_failed);
				}
				break;
		}
	}

	String whoseTurnIsIt(String envTurn) {
		if (envTurn == ServerMessage.turn_of_bottom_player)
			return agents.get(players[0]).username;
		return agents.get(players[1]).username;
	}

	public static void main(String[] args) {
		Simulator a = new Simulator(2);  // Paper Soccer specific
		a.setEnvironment(new Environment(10, 8));  // Paper Soccer specific
		a.startWhenReady();
	}
}
