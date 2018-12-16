package agent;

import common.ClientMessage;
import common.ServerMessage;
import common.Watchable;
import common.Watcher;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;

public class AgentAPI {
	Socket socket;
	private PrintWriter pw;
	private Scanner sc;
	private String username;
	private boolean player = false;
	int side;
	int board[][];
	private Watchable<GameState> gameState;

	AgentAPI(Watcher<GameState> w) throws IOException {
		socket = new Socket("localhost", 10000);
		pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
		sc = new Scanner(socket.getInputStream());

		gameState = new Watchable<>(GameState.GAME_PREPARING);
		gameState.setWatcher(w);

		authentication();
	}

	void authentication() {
		do {
//			setUsername();
			send(ClientMessage.agent_authentication);
			send(username);
		} while (receive() == ServerMessage.authentication_approval);
	}

	boolean changeAgentType() {
		String type;
		if (isPlayer()) {
			setSide();
			type = "player " + side;
		} else
			type = "observer";
		send(ClientMessage.agent_type + " " + type);
		if (receive() == ServerMessage.change_agent_type_approval)
			return true;
		return false;
	}


	// to set followings using UIs depends on agent types: ai / (textual/graphical) human / (textual/graphical) observer
	void setUsername(String username) {
		this.username = username;
	}

	void setPlayer(boolean palayer) {

	}

	void setSide() {

	}

	void joinGame() {

	}

	void leaveGame() {

	}

	void start() {
		new Thread(() -> {
			this.observe();
		}).start();
	}

	private void observe() {
		while (true) {
			String serverBroadcast = receive();
			if (Objects.equals(serverBroadcast, ServerMessage.world_broadcast)) {
				board = boardParser(receive());
			} else if (Objects.equals(serverBroadcast, ServerMessage.turn_broadcast)) {
				String turn = receive();
				if (Objects.equals(turn, ServerMessage.game_finished)) {
					gameState.setValue(GameState.GAME_FINISHED);
					break;
				} else if (turn == username)
					gameState.setValue(GameState.YOUR_TURN);
				else
					gameState.setValue(GameState.OTHERS_TURN);
			}
		}
	}

	void makeDecision() {

	}

	private int[][] boardParser(String receive) {
		int tmp[][] = new int[11][9];
		String[] lines = receive.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String[] line = lines[i].split(" ");
			for (int j = 0; j < line.length; j++)
				tmp[i][j] = Integer.parseInt(line[j]);
		}
		return tmp;
	}


	boolean isPlayer() {
		return player;
	}

	void send(String s) {
		pw.println(s);
	}

	String receive() {
		return sc.nextLine();
	}
}
