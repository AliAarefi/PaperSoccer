package papersoccer.common;

import java.io.IOException;
import java.io.OutputStream;

public class Logger {
	private String namespace;
	private OutputStream out;
	private int level = LoggerLevel.DEBUG;

	public Logger(String namespace) {
		this.out = System.out;
		setNamespace(namespace);
	}

	public Logger(String namespace, OutputStream out) {
		this.out = out;
		setNamespace(namespace);
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public boolean isLoggable(int level) {
		return level >= this.level;
	}

	public void debug(int indent, String message) {
		if (isLoggable(LoggerLevel.DEBUG)) {
			try {
				out.write(String.format("[%s] ", namespace).getBytes());
				while (indent-- != 0)
					out.write('\t');
				out.write(message.getBytes());
				out.write('\n');
			} catch (IOException ignored) {
			}
		}
	}

	public void d(int indent, String message) {
		debug(indent, message);
	}
}
