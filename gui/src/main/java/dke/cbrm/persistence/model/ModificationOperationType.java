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
    DELETE_PARAMETER(1),
    MODIFY_PARAMETER(2),
    ADD_RULE(3),
    DELETE_RULE(4),
    MODIFY_RULE(5),
    ADD_CONTEXT(6),
    DELETE_CONTEXT(7),
    MODIFY_CONTEXT(8),
    ADD_CONTEXT_CLASS(9),
    DELETE_CONTEXT_CLASS(10),
    MODIFY_CONTEXT_CLASS(11),
    MODIFY_BUSINESS_CASE(12),
    MODIFY_BUSINESS_CASE_CLASS(13);

    private ModificationOperationType(int id) {
	this.id = id;
    }

    private int id;
}
