package papersoccer.simulator;

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
	String username;
	final UUID id;
	private boolean player = false;
	int side;

	Agent(Socket s, UUID uuid) throws IOException {
		id = uuid;

		socket = s;
		pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
		sc = new Scanner(s.getInputStream());

		try {
			while (true) {
				Message m = new Message(id, receive());
				MessageQueue.getInstance().put(m);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void setPlayer(boolean player, int side) {
		this.player = player;
		this.side = side;
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
