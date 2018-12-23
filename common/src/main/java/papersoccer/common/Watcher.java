package papersoccer.common;

public interface Watcher<T> {
	void valueChanged(Watchable<T> obj, T oldValue, T newValue);
}
