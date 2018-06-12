package dke.cbrm.gui;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vaadin.data.TreeData;
import com.vaadin.ui.TreeGrid;

import dke.cbrm.business.CbrmService;
import dke.cbrm.persistence.model.Context;
import dke.cbrm.persistence.model.Parameter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TreeGridCommons {

    private final CbrmService cbrmService;

    /**
     * Recursively iterates with the first given @{link
     * Context}-parent-Object through resulting Object-Tree and adds them
     * to @{link TreeData}
     * 
     * @param data
     *            the data to be displayed
     * @param parent
     *            the Context to be iterated through
     */
    public void addItemsToTreeGrid(TreeData<Context> data, Context parent,
	    TreeGrid<Context> treeGrid, boolean add) {

	parent = cbrmService.getChildren(parent);
	parent = cbrmService.loadParametersOfContext(parent);

	if (add) {
	    data.addItem(null, parent);
	}

	if (!parent.getChildren().isEmpty()) {
	    data.addItems(parent, parent.getChildren());
	    for (Context child : (Set<Context>) parent.getChildren()) {
		addItemsToTreeGrid(data, child, treeGrid, false);
	    }
	}

	treeGrid.expand(parent);
    }

    /**
     * Recursively iterates with the first given @{link
     * Context}-parent-Object through resulting Object-Tree and adds them
     * to @{link TreeData}
     * 
     * @param data
     *            the data to be displayed
     * @param parent
     *            the Context to be iterated through
     */
    public void addItemsToTreeGrid(TreeData<Parameter> data, Parameter parent,
	    TreeGrid<Parameter> treeGrid, boolean add) {

	parent = cbrmService.getChildren((Parameter) parent);

	if (add) {
	    data.addItem(null, parent);
	}

	if (!parent.getChildren().isEmpty()) {
	    data.addItems(parent, parent.getChildren());
	    for (Parameter child : (Set<Parameter>) parent.getChildren()) {
		addItemsToTreeGrid(data, child, treeGrid, false);
	    }
	}

	treeGrid.expand(parent);
    }
}
