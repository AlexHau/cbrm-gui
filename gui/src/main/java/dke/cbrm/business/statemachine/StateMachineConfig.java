package dke.cbrm.business.statemachine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.data.RepositoryState;
import org.springframework.statemachine.data.RepositoryStateMachineModelFactory;
import org.springframework.statemachine.data.RepositoryTransition;
import org.springframework.statemachine.data.StateRepository;
import org.springframework.statemachine.data.TransitionRepository;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig
	extends StateMachineConfigurerAdapter<String, String> {

    @Autowired
    private StateRepository<? extends RepositoryState> stateRepository;

    @Autowired
    private TransitionRepository<? extends RepositoryTransition> transitionRepository;

    @Override
    public void configure(StateMachineModelConfigurer<String, String> model)
	    throws Exception {
	model.withModel().factory(modelFactory());
    }

    @Bean
    public StateMachineModelFactory<String, String> modelFactory() {
	// Iterator<? extends RepositoryState> stateIter =
	// stateRepository.findAll().iterator();
	// while (stateIter.hasNext()) {
	// RepositoryState state = stateIter.next();
	// System.out.println(
	// "**************************************************");
	// System.out.println("state.getMachineId() " + state.getMachineId());
	// System.out.println("state.getState() " + state.getState());
	// System.out.println("state.isInitial() " + state.isInitial());
	//
	// }
	// Iterator<? extends RepositoryTransition> tansIter =
	// transitionRepository.findAll().iterator();
	// while (tansIter.hasNext()) {
	// RepositoryTransition trans = tansIter.next();
	// System.out.println(
	// "**************************************************");
	// System.out.println("state.getMachineId() " + trans.getMachineId());
	// System.out.println("trans.getEvent() " + trans.getEvent());
	// System.out.println("trans.getSource() " + trans.getSource());
	// System.out.println("trans.getTarget() " + trans.getTarget());
	//
	// }

	return new RepositoryStateMachineModelFactory(stateRepository,
		transitionRepository);
    }

    @Bean
    public StateMachineListener<CbrmState, CbrmEvent> listener() {
	return new StateMachineListenerAdapter<CbrmState, CbrmEvent>() {
	    @Override
	    public void stateChanged(State<CbrmState, CbrmEvent> from,
		    State<CbrmState, CbrmEvent> to) {
		System.out.println("State change to " + to.getId());
	    }
	};
    }
}
