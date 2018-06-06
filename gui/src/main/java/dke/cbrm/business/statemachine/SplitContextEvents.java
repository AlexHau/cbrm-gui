package dke.cbrm.business.statemachine;

public enum SplitContextEvents implements CbrmEvent {

    REQUEST_FOR_CHANGE,

    ADD_PARAMETER,

    ADD_CONTEXT,

    ASSIGN_USER,

    DELETE_RULE,

    ADD_RULE,

    WAIT_FOR_APPROVAL,

    APPROVE;
}
