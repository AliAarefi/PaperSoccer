package common;

public interface Watcher<T> {
	void valueChanged(Watchable obj, T oldValue, T newValue);
}
