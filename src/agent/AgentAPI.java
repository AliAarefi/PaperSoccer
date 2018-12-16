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
	private Socket socket;
	private PrintWriter pw;
	private Scanner sc;
	private String username;
	private boolean player = false;
	public int side;
	public int board[][];
	private Watchable<GameState> gameState;

	public AgentAPI(Watcher<GameState> w) throws IOException {
		socket = new Socket("localhost", 10000);
		pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
		sc = new Scanner(socket.getInputStream());

		gameState = new Watchable<>(GameState.GAME_PREPARING);
		gameState.setWatcher(w);
	}

	private void send(String s) {
		pw.println(s);
	}

	private String receive() {
		return sc.nextLine();
	}

	private boolean setUsername(String username) {
		send(username);
		if (Objects.equals(receive(), ServerMessage.authentication_approved)) {
			this.username = username;
			return true;
		} else return false;

	}

	public boolean authenticate(String username) {
		send(ClientMessage.agent_authentication);
		return setUsername(username);
	}

	private void setPlayer(int side) {
		this.player = true;
		this.side = side;
	}

	public boolean isPlayer() {
		return player;
	}

	private boolean changeAgentType(int side) {
		String type;
		if (isPlayer()) {
			setPlayer(side);
			type = "player " + side;
		} else
			type = "observer";
		send(ClientMessage.agent_type + " " + type);
		return Objects.equals(receive(), ServerMessage.change_agent_type_approval);
	}

	public boolean joinGame() {
		return true;  // TODO
	}

	public boolean leaveGame() {
		return true;  // TODO
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
				} else if (Objects.equals(turn, username))
					gameState.setValue(GameState.YOUR_TURN);
				else
					gameState.setValue(GameState.OTHERS_TURN);
			}
		}
	}

	public boolean setDecision(int source, int destinaiton) {
		if (gameState.getValue() == GameState.YOUR_TURN) {
			send(ClientMessage.action_request);
			return Objects.equals(receive(), ServerMessage.action_accepted);
		}
		return false;
	}
}
