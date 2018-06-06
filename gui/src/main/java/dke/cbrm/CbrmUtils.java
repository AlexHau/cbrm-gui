package dke.cbrm;

import com.vaadin.ui.Grid;

import dke.cbrm.persistence.model.ModificationOperation;
import dke.cbrm.persistence.model.Parameter;
import dke.cbrm.persistence.model.Role;
import dke.cbrm.persistence.model.User;

public class CbrmUtils {

    /**
     * Formats / removes Columns from Grid displaying {@link User}s
     * 
     * @param table
     *            the Grid to be formatted
     */
    public static void removeColumnsFromUser(Grid<User> table) {
	table.removeColumn("id");
	table.removeColumn("roles");
	table.removeColumn("password");
	table.removeColumn("enabled");
	table.removeColumn("tokenExpired");
    }

    public static void removeColumnsFromRole(Grid<Role> userRolesGrid) {
	userRolesGrid.removeColumn("id");
	userRolesGrid.removeColumn("users");
	userRolesGrid.removeColumn("privileges");
    }

    public static void removeColumnsFromModificationoperation(Grid<ModificationOperation> modOpsGrid) {
	modOpsGrid.removeColumn("id");
	modOpsGrid.removeColumn("approvals");
	modOpsGrid.removeColumn("modifiedAt");
	modOpsGrid.removeColumn("ruleAffected");
	modOpsGrid.removeColumn("parameterAffected");
	modOpsGrid.removeColumn("contextAffected");
    }
    
}
