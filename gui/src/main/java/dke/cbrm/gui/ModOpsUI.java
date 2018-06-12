package dke.cbrm.gui;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;

import dke.cbrm.CbrmUtils;
import dke.cbrm.business.CbrmService;
import dke.cbrm.business.PermissionService;
import dke.cbrm.persistence.model.ModificationOperation;
import lombok.RequiredArgsConstructor;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ModOpsUI extends HorizontalLayout {

    private static final long serialVersionUID = -626737363117332907L;

    private Set<ModificationOperation> modificationOperationsInterestedIn =
	    new HashSet<ModificationOperation>();

    private final CbrmService cbrmService;

    private final PermissionService permissionService;

    private Grid<ModificationOperation> modOpsGrid;

    private Authentication loggedInUser;

    public void initialize(Authentication loggedInUser) {
	this.loggedInUser = loggedInUser;
	this.modOpsGrid =
		new Grid<ModificationOperation>(ModificationOperation.class);

	Set<ModificationOperation> modOpsForUser = Sets.newHashSet(cbrmService
		.getModificationOperationsForUser(this.loggedInUser.getName()));

	this.modOpsGrid
		.setItems(modificationOperationsInterestedIn = modOpsForUser);
	this.modOpsGrid.setCaption("Open Modification Operations for Approval");
	CbrmUtils.removeColumnsFromModificationoperation(modOpsGrid);

	this.addComponent(modOpsGrid);
	this.setSizeFull();
	this.modOpsGrid.setSizeFull();
    }

    public void addModificationOperation(ModificationOperation modOp) {
	synchronized (modificationOperationsInterestedIn) {
	    modificationOperationsInterestedIn.add(modOp);
	}
    }

}
