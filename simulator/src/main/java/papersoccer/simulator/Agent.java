package papersoccer.simulator;

import java.util.UUID;

abstract class Agent<Socket> {
	Socket socket;
	String username;
	final UUID id;
	private boolean player = false;
	int side = -1;

	Agent(Socket s, UUID uuid) {
		id = uuid;
		socket = s;
	}

	void setPlayer(int side) {
		this.player = true;
		this.side = side;
	}

	void unsetPlayer() {
		this.player = false;
		this.side = -1;
	}

	boolean isPlayer() {
		return player;
	}

	abstract void send(String s);
}
