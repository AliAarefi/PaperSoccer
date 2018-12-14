package common;

public class Watchable<T> {
	private Watcher<T> watcher;
	private T value;

	public Watchable(T initial) {
		this.value = initial;
	}

	public void setWatcher(Watcher<T> watcher) {
		this.watcher = watcher;
	}

	public void setValue(T value) {
		if (this.value == value) return;
		if (this.watcher != null) watcher.valueChanged(this, this.value, value);
		this.value = value;
	}

	public T getValue() {
		return value;
	}
}
