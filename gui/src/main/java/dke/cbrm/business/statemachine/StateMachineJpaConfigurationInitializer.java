package dke.cbrm.business.statemachine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.data.StateRepository;
import org.springframework.statemachine.data.TransitionRepository;
import org.springframework.statemachine.data.jpa.JpaRepositoryState;
import org.springframework.statemachine.data.jpa.JpaRepositoryTransition;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StateMachineJpaConfigurationInitializer {

    private final StateRepository<JpaRepositoryState> stateRepository;

    private final TransitionRepository<JpaRepositoryTransition> transitionRepository;

    private final String SPLIT_MACHINE_ID = "splitContextStateMachine";

    public void initializeJpaStateMachineRepository() {
	JpaRepositoryState addingParameterState =
		new JpaRepositoryState(SPLIT_MACHINE_ID, null,
			SplitContextStates.ADDING_PARAMETER.name(), true);
	JpaRepositoryState addingContextState =
		new JpaRepositoryState(SPLIT_MACHINE_ID, null,
			SplitContextStates.ADDING_CONTEXT.name(), false);
	JpaRepositoryState addingRuleState =
		new JpaRepositoryState(SPLIT_MACHINE_ID, null,
			SplitContextStates.ADDING_RULE.name(), false);
	JpaRepositoryState deletingRuleState =
		new JpaRepositoryState(SPLIT_MACHINE_ID, null,
			SplitContextStates.DELETING_RULE.name(), false);
	JpaRepositoryState waitingForApprovalState =
		new JpaRepositoryState(SPLIT_MACHINE_ID, null,
			SplitContextStates.WAITING_FOR_APPROVAL.name(), false);
	JpaRepositoryState approvedState =
		new JpaRepositoryState(SPLIT_MACHINE_ID, null,
			SplitContextStates.APPROVED.name(), false);

	stateRepository.save(addingParameterState);
	stateRepository.save(addingContextState);
	stateRepository.save(addingRuleState);
	stateRepository.save(deletingRuleState);
	stateRepository.save(waitingForApprovalState);
	stateRepository.save(approvedState);

	JpaRepositoryTransition transitionAddParameterToAddContext =
		new JpaRepositoryTransition(SPLIT_MACHINE_ID,
			addingParameterState, addingContextState,
			SplitContextEvents.ADD_CONTEXT.name());

	JpaRepositoryTransition transitionContexToAddRule =
		new JpaRepositoryTransition(SPLIT_MACHINE_ID,
			addingContextState, addingRuleState,
			SplitContextEvents.ADD_RULE.name());

	JpaRepositoryTransition transitionAddRuleToDeleteRule =
		new JpaRepositoryTransition(SPLIT_MACHINE_ID, addingRuleState,
			deletingRuleState,
			SplitContextEvents.DELETE_RULE.name());

	JpaRepositoryTransition transitionDeleteToWaitingForApproval =
		new JpaRepositoryTransition(SPLIT_MACHINE_ID, deletingRuleState,
			waitingForApprovalState,
			SplitContextEvents.WAIT_FOR_APPROVAL.name());

	JpaRepositoryTransition transitionWaitingForApprovalToApproved =
		new JpaRepositoryTransition(SPLIT_MACHINE_ID,
			waitingForApprovalState, approvedState,
			SplitContextEvents.APPROVE.name());

	transitionRepository.save(transitionAddParameterToAddContext);
	// transitionRepository.save(transitionRequestToAddContext);
	transitionRepository.save(transitionContexToAddRule);
	transitionRepository.save(transitionAddRuleToDeleteRule);
	transitionRepository.save(transitionDeleteToWaitingForApproval);
	transitionRepository.save(transitionWaitingForApprovalToApproved);

    }

}
