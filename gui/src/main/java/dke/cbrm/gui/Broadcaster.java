package dke.cbrm.gui;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vaadin.ui.UI;

import dke.cbrm.persistence.model.ModificationOperation;
import dke.cbrm.persistence.model.User;
import lombok.RequiredArgsConstructor;

/**
 * 
 * This class is responsible for registering all @link{CbrmUI}s
 * interested in push notifications and providing broadcast mechanism
 * for messages (i.e.: Modification-Operation Events)
 *
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
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

    public static synchronized void broadcast(final ModificationOperation modOp,
	    Set<User> affectedUsers) {

	for (final BroadcastListener listener : listeners)
	    executorService.execute(() -> {
		String searchedUer =
			((CbrmUI) listener).getLoggedInUser().getName();
		for (User user : affectedUsers) {
		    if (user.getUserName().equals(searchedUer)) {
			listener.receiveBroadcast(modOp);
			((UI) listener).push();
		    }
		}
	    });
    }
}
