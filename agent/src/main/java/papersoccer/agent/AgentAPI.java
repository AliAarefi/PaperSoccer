package papersoccer.agent;

import papersoccer.common.ClientMessage;
import papersoccer.common.ServerMessage;
import papersoccer.common.Watchable;
import papersoccer.common.Watcher;

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
	public int ballPosition;
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
		send(ClientMessage.agent_authentication + " " + username);
		if (Objects.equals(receive(), ServerMessage.authentication_approved)) {
			this.username = username;
			return true;
		} else return false;

	}

	public boolean authenticate(String username) {
		return setUsername(username);
	}

	public boolean isPlayer() {
		return player;
	}

	private void changeAgentType(int side) {  // if side == 0 change sth locally, (server side by join/leave)
		if (side == 0)
			this.player = false;
		else {
			this.player = true;
			this.side = side;
		}
	}

	public boolean joinGame(int side) {
		send(ClientMessage.join_request + " " + side);
		String response = receive();
		if (Objects.equals(response, ServerMessage.join_accepted)) {
			changeAgentType(side);
			return true;
		} else if (Objects.equals(response, ServerMessage.error_players_full)) {
			return false;
		} else if (Objects.equals(response, ServerMessage.join_failed)) {
			return false;  // TODO change return type: selected side is full but another side is empty (=SM.join_failed)
		}
		return false;
	}

	public boolean leaveGame() {
		send(ClientMessage.leave_request);
		if (Objects.equals(receive(), ServerMessage.leave_accepted)) {
			changeAgentType(0);
			return true;
		}
		return false;
	}

	private int[][] boardParser(String receive) {
		int[][] tmp = new int[11][9];  // FIXME make it dynamic by counting '\n' and ' '
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
			} else if (Objects.equals(serverBroadcast, ServerMessage.ball_position_broadcast)) {
				ballPosition = Integer.parseInt(receive());
			}
		}
	}

	public boolean setDecision(int source, int destination) {
		if (gameState.getValue() == GameState.YOUR_TURN) {
			send(ClientMessage.action_request + " " + source + " " + destination);
			return Objects.equals(receive(), ServerMessage.action_accepted);
		}
		return false;
	}
}
