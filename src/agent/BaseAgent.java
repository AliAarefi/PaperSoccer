package agent;

import common.ClientMessage;
import common.ServerMessage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public abstract class BaseAgent {
	Socket socket;
	private PrintWriter pw;
	private Scanner sc;
	private String username;
	private boolean player = false;
	int side;
	int board[][];

	BaseAgent() throws IOException {
		socket = new Socket("localhost", 10000);
		pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
		sc = new Scanner(socket.getInputStream());
		authentication();
	}

	void authentication() {
		do {
			setUsername();
			send(ClientMessage.agent_authentication);
			send(username);
		} while (receive() == ServerMessage.authentication_aproval);
	}

	boolean changeAgentType() {
		String type;
		if (isPlayer()) {
			setSide();
			type = "player " + side;
		} else
			type = "observer";
		send(ClientMessage.agent_type + " " + type);
		if (receive() == ServerMessage.change_agent_type_aproval)
			return true;
		return false;
	}


	// to set followings using UIs depends on agent types: ai / (textual/graphical) human / (textual/graphical) observer
	abstract void setUsername();

	abstract void setPlayer();

	abstract void setSide();

	abstract void joinGame();

	abstract void leaveGame();

	void start() {
		new Thread(() -> {
			this.observe();
		}).start();
	}

	private void observe() {
		while (true) {
			if (receive() == ServerMessage.world_broadcast) {
				board = boardParser(receive());
			} else if (receive() == ServerMessage.turn_broadcast) {
				String turn = receive();
				if (turn == "Finish")
					break;
				else if (turn == username) {
					new Thread(() -> {
						this.makeDecision();
					}).start();
				}
			}
		}
	}

	abstract void makeDecision();

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
