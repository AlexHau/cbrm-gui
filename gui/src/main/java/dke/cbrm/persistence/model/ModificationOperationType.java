package dke.cbrm.persistence.model;

import lombok.Getter;

/**
 * @author ahauer
 * 
 *         ENUM representing all possible Atomic Modification
 *         Operations in CBRM
 *
 */
@Getter
public enum ModificationOperationType {

    ADD_PARAMETER(0),
    ADD_PARAMETER_VALUE(1),
    DELETE_PARAMETER(2),
    DELETE_PARAMETER_VALUE(3),
    MODIFY_PARAMETER(4),
    ADD_RULE(5),
    DELETE_RULE(6),
    ADD_CONTEXT(7),
    DELETE_CONTEXT(8),
    ADD_CONTEXT_CLASS(9),
    DELETE_CONTEXT_CLASS(10);

    private ModificationOperationType(int id) {
	this.id = id;
    }

    private int id;
}
