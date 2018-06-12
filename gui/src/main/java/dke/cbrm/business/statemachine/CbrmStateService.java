package dke.cbrm.business.statemachine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;

/**
 * @author ahauer
 *
 *         Service responsible for management of state-machines
 *         depicting composed modification operations
 */
@Service
public class CbrmStateService {

    public class CbrmStateMachine {
	public static final String SPLIT_CONTEXT_STATE_MACHINE =
		"splitContextStateMachine";
    }

    private StateMachine<String, String> stateMachine;

    @Autowired
    private StateMachineFactory<String, String> stateMachineFactory;

    public void runStateMachine(String stateMachineId)
	    throws StateMachineRunningException {
	if (stateMachineIsRunning()) {
	    throw new StateMachineRunningException(
		    stateMachine.getId() + " is still running");
	}
	stateMachine = stateMachineFactory.getStateMachine(stateMachineId);
	stateMachine.start();
    }

    public void sendEvent(String event) {
	stateMachine.sendEvent(event);
	System.out.println(
		"current state: " + stateMachine.getState().toString());
    }

    public boolean stateMachineIsRunning() {
	if (stateMachine == null || stateMachine.isComplete()) {
	    return false;
	} else
	    return true;
    }
}
