package dke.cbrm.business.statemachine;

public enum ModificationOperationStates {
    REQUEST_FOR_CHANGE(0),

    CHANGE(1),

    AWAITING_APPROVAL(2),

    APPROVED(3);

    ModificationOperationStates(int id) {
	this.setId(id);
    }

    public int getId() {
	return id;
    }

    public void setId(int id) {
	this.id = id;
    }

    private int id;
}
