package dke.cbrm.gui;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.vaadin.ui.UI;

import dke.cbrm.persistence.model.ModificationOperation;

/**
 * 
 * This class is responsible for registering all @link{CbrmUI}s
 * interested in push notifications and providing broadcast mechanism
 * for messages (i.e.: Modification-Operation Events)
 *
 */
public class Broadcaster implements Serializable {

    private static final long serialVersionUID = 1075025408367099462L;

    static ExecutorService executorService =
	    Executors.newSingleThreadExecutor();

    public interface BroadcastListener {
	void receiveBroadcast(ModificationOperation message);
    }

    private static LinkedList<BroadcastListener> listeners =
	    new LinkedList<BroadcastListener>();

    public static synchronized void register(BroadcastListener listener) {
	listeners.add(listener);
    }

    public static synchronized void unregister(BroadcastListener listener) {
	listeners.remove(listener);
    }

    public static synchronized void broadcast(
	    final ModificationOperation message) {
	for (final BroadcastListener listener : listeners)
	    executorService.execute(() -> {
		listener.receiveBroadcast(message);
		((UI) listener).push();
	    });
    }
}
