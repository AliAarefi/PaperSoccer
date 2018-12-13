package common;

public class Logger {
	private String namespace;

	public Logger(String namespace) {
		this.namespace = namespace;
	}

	public void d(int indent, String message) {
		System.out.print(String.format("[%s] ", namespace));
		while (indent-- != 0)
			System.out.print('\t');
		System.out.println(message);
	}
}
