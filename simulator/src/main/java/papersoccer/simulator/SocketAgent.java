package papersoccer.simulator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;

class SocketAgent extends Agent<Socket> {
	private PrintWriter pw;
	private Scanner sc;

	SocketAgent(Socket s, UUID uuid) throws IOException {
		super(s, uuid);

		pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
		sc = new Scanner(s.getInputStream());

		new Thread(() -> {
			try {
				while (true) {
					Message m = new Message(id, sc.nextLine());
					MessageQueue.getInstance().put(m);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}

	@Override
	boolean send(String s) {
		pw.println(s);
		return true;
	}
}
