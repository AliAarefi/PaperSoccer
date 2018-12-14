package Simulator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;

class Agent {
	private Socket socket;
	private PrintWriter pw;
	private Scanner sc;
	private String username;
	private UUID id;
	private boolean player = false;
	int side;

	Agent(Socket s, UUID uuid) throws IOException {
		id = uuid;

		socket = s;
		pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
		sc = new Scanner(s.getInputStream());
	}

	void initialize() {
		username = receive();

		String[] type = receive().split(" ");
		if (type[0].equals("player")) {
			player = true;  // FIXME Must not set the player before checking with the simulator
			side = Integer.parseInt(type[1]) - 1;
		}
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
