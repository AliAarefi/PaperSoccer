package papersoccer.simulator;

import papersoccer.common.ClientMessage;
import papersoccer.common.Logger;
import papersoccer.common.ServerMessage;
import papersoccer.common.Watchable;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class Simulator {
	private Logger log = new Logger("Simulator");
	private SimulatorSocketServer server;
	private SimulatorWebSocketServer serverWeb;
	private Environment environment;
	private Map<UUID, Agent> agents;
	private UUID[] players;
	private Watchable<Boolean> gameReady;
	private boolean simulating = true;
	private boolean gameRunning = false;
	private Thread messageHandler;
	private String turn;

	Simulator(int number_of_players) {
		agents = new ConcurrentHashMap<>();
		players = new UUID[number_of_players];

		gameReady = new Watchable<>(Boolean.FALSE);

		int port;
		while (true) {
			try {
				port = new Random().nextInt(60000) + 5000;  // Find an open port from 5000 to 65000 (exclusive)
				server = new SimulatorSocketServer(port, agents);
				serverWeb = new SimulatorWebSocketServer(port + 1, agents);
				server.start();
				serverWeb.start();
				break;
			} catch (IOException ignored) {
			}
		}

		messageHandler = new Thread(() -> {
			try {
				while (simulating) {
					Message m = MessageQueue.getInstance().take();
					log.d(0, String.format("New message received from agent %s", m.id.toString()));
					handle(m);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		messageHandler.start();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			log.d(0, "Shutting down the simulator...");
			simulating = false;
			try {
				server.stop();
				serverWeb.stop();
			} catch (IOException | InterruptedException ignored) {
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
		environment.clear();
	}

	private void start() {
		environment.clear();
		gameRunning = true;
		turn = whoseTurnIsIt(environment.getTurn());
		broadcastGameToAll();
	}

	private void pause() {
		gameRunning = false;
		broadcastPauseToAll();
	}

	void startWhenReady() {
		gameReady.setWatcher((obj, oldValue, newValue) -> {
			if (newValue == Boolean.TRUE) start();
		});
	}

	void dontStartWhenReady() {
		gameReady.removeWatcher();
	}

	private void handle(Message m) {
		Agent agent = agents.get(m.id);
		String[] message = m.message.split(" ", 2);

		switch (message[0]) {
			case ClientMessage.agent_authentication:
				if (message.length == 2) {
					// TODO check username uniqueness
					agent.username = message[1];
					agent.send(ServerMessage.authentication_approved);
					log.d(0, String.format("Agent %s successfully authenticated.", agent.id.toString()));
					if (gameRunning)
						broadcastGame(agent);
					break;
				}
				agent.send(ServerMessage.authentication_failed);
				break;

			case ClientMessage.join_request:
				if (!gameRunning && message.length == 2) {
					int side = Integer.parseInt(message[1]) - 1;
					if (players[side] == null) {
						agent.setPlayer(side);
						players[side] = agent.id;
						log.d(0, String.format("Agent %s is set to player %d.", agent.id.toString(), agent.side + 1));
						checkGameReady();
					} else {
						if (players[players.length - 1 - side] == null)
							agent.send(ServerMessage.join_failed);
						else
							agent.send(ServerMessage.error_players_full);
					}
					break;
				}
				agent.send(ServerMessage.join_failed);
				break;

			case ClientMessage.leave_request:
				agent.send(ServerMessage.leave_accepted);
				players[agent.side] = null;
				agent.unsetPlayer();
				if (gameRunning)
					pause();
				log.d(0, String.format("Agent %s left the game.", agent.id.toString()));
				break;

			case ClientMessage.action_request:
				message = message[1].split(" ");
				if (gameRunning && message.length == 2) {
					if (environment.doAction(Integer.parseInt(message[0]), Integer.parseInt(message[1]))) {
						agent.send(ServerMessage.action_accepted);
						log.d(0, String.format("Agent %s acted.", agent.id.toString()));
						turn = whoseTurnIsIt(environment.getTurn());
						broadcastGameToAll();
						break;
					}
				}
				agent.send(ServerMessage.action_failed);
				break;

			case ClientMessage.disconnected:
				agents.remove(agent.id);
		}
	}

	private void broadcastGameToAll() {
		agents.values().parallelStream().forEach(this::broadcastGame);
	}

	private void broadcastPauseToAll() {
		agents.values().parallelStream().forEach(agent -> agent.send(ServerMessage.game_paused));
	}

	private void broadcastGame(Agent agent) {
		// Broadcast world, ballPosition & turn
		agent.send(ServerMessage.world_broadcast + " " + environment.convertToString());
		agent.send(ServerMessage.ball_position_broadcast + " " + environment.getBallPosition());
		agent.send(ServerMessage.turn_broadcast + " " + turn);
	}

	private String whoseTurnIsIt(String envTurn) {
		return Objects.equals(envTurn, ServerMessage.turn_of_bottom_player) ? agents.get(players[0]).username : agents.get(players[1]).username;
	}

	public static void main(String[] args) {
		Simulator a = new Simulator(2);  // Paper Soccer specific
		a.setEnvironment(new Environment(10, 8));  // Paper Soccer specific
		a.startWhenReady();
	}
}
