package dke.cbrm.gui;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import com.google.common.collect.Sets;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.event.selection.SelectionListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;

import dke.cbrm.CbrmUtils;
import dke.cbrm.business.PermissionService;
import dke.cbrm.persistence.model.Role;
import dke.cbrm.persistence.model.User;
import lombok.RequiredArgsConstructor;

/**
 * @author ahauer
 * 
 *         This class is instantiated for
 *         User-Role @link{CbrmConstants.UserRoles.ROLE_REPO_ADMIN} in
 *         order to manage users´ roles
 *
 */
@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserManagementTab extends VerticalLayout {

    private static final long serialVersionUID = -5953475469071818961L;

    private final PermissionService permissionService;

    private Grid<User> usersGrid;

    private Grid<Role> userRolesOwnedGrid;

    private Grid<Role> userRolesNotOwnedGrid;

    private Button addRoleToUser = new Button("Add Role", this::addRole),
	    deleteRoleFromUser = new Button("Delete Role", this::deleteRole);

    private Set<User> users;

    private Set<Role> userRolesOwned;

    private User userSelected;

    private Role roleNotOwnedByUser, roleOwnedAndSelected;

    public void instantiate() {
	users = Sets.newHashSet(permissionService.getAllExistingUsers());

	usersGrid = new Grid<User>(User.class);
	usersGrid.setCaption("Allowed Users");
	usersGrid.setSizeFull();
	usersGrid.setSelectionMode(SelectionMode.SINGLE);
	usersGrid.setItems(users);
	CbrmUtils.removeColumnsFromUser(usersGrid);

	usersGrid.addSelectionListener(new SelectionListener<User>() {

	    private static final long serialVersionUID = 2524716704186504179L;

	    @Override
	    public void selectionChange(SelectionEvent<User> event) {
		if (event.getFirstSelectedItem().isPresent()) {
		    userSelected = event.getFirstSelectedItem().get();
		    refreshUserRoles(userSelected);
		}
	    }

	});

	userRolesOwnedGrid = new Grid<Role>(Role.class);
	userRolesOwnedGrid.setCaption("Roles of User");
	userRolesOwnedGrid.setSizeFull();
	CbrmUtils.removeColumnsFromRole(userRolesOwnedGrid);
	userRolesOwnedGrid.addSelectionListener(new SelectionListener<Role>() {

	    private static final long serialVersionUID = 6316935588917212128L;

	    @Override
	    public void selectionChange(SelectionEvent<Role> event) {
		if (event.getFirstSelectedItem().isPresent()) {
		    roleOwnedAndSelected = event.getFirstSelectedItem().get();
		}
	    }

	});

	userRolesNotOwnedGrid = new Grid<Role>(Role.class);
	userRolesNotOwnedGrid.setCaption("Roles not owned by Users");
	userRolesNotOwnedGrid.setSizeFull();
	CbrmUtils.removeColumnsFromRole(userRolesNotOwnedGrid);
	userRolesNotOwnedGrid
		.addSelectionListener(new SelectionListener<Role>() {

		    private static final long serialVersionUID =
			    6316935588917212128L;

		    @Override
		    public void selectionChange(SelectionEvent<Role> event) {
			if (event.getFirstSelectedItem().isPresent()) {
			    roleNotOwnedByUser =
				    event.getFirstSelectedItem().get();
			}
		    }

		});
	HorizontalLayout hGridLaayout =
		new HorizontalLayout(userRolesOwnedGrid, userRolesNotOwnedGrid);
	this.addComponent((com.vaadin.ui.Component) hGridLaayout);
	HorizontalLayout hButtonLayout =
		new HorizontalLayout(addRoleToUser, deleteRoleFromUser);
	this.addComponent(hButtonLayout);
	this.addComponent((com.vaadin.ui.Component) usersGrid);

	this.setSizeFull();
    }

    private void addRole(Button.ClickEvent e) {
	StringBuffer strBuffer = new StringBuffer();
	if (this.userSelected == null)
	    strBuffer.append("User has to be selected for Adding a Role")
		    .append(System.lineSeparator());
	if (this.roleNotOwnedByUser == null)
	    strBuffer
		    .append("Not owned User Role has to be selected for Adding")
		    .append(System.lineSeparator());

	if (strBuffer.length() > 0) {
	    Notification.show("Adding Role impossible: ", strBuffer.toString(),
		    Notification.Type.ERROR_MESSAGE);
	} else {
	    userSelected = permissionService.loadRolesOfAllowedUsers(userSelected);
	    userSelected.getRoles().add(roleNotOwnedByUser);
	    permissionService.updateUser(userSelected);
	    refreshUserRoles(userSelected);
	}
    }

    private void deleteRole(Button.ClickEvent e) {
	StringBuffer strBuffer = new StringBuffer();
	if (this.userSelected == null)
	    strBuffer.append("User has to be selected for Deleting a Role")
		    .append(System.lineSeparator());
	if (this.roleOwnedAndSelected == null)
	    strBuffer.append(
		    "An owned User Role has to be selected for Deleting")
		    .append(System.lineSeparator());

	if (strBuffer.length() > 0) {
	    Notification.show("Deleting Role impossible: ",
		    strBuffer.toString(), Notification.Type.ERROR_MESSAGE);
	} else {
	    userSelected = permissionService.loadRolesOfAllowedUsers(userSelected);
	    userSelected.getRoles().remove(roleOwnedAndSelected);
	    permissionService.updateUser(userSelected);
	    refreshUserRoles(userSelected);
	}
    }

    private void refreshUserRoles(User user) {
	userRolesOwned = Sets
		.newHashSet(permissionService.getRolesForUser(user.getId()));
	userRolesOwnedGrid.setItems(userRolesOwned);

	Set<Role> userRolesNotOwned = Sets
		.newHashSet(permissionService.getRolesNotForUser(user.getId()));
	userRolesNotOwnedGrid.setItems(userRolesNotOwned);
    }

}
