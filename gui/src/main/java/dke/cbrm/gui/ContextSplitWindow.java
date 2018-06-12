package dke.cbrm.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.event.selection.SelectionListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TreeGrid;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import dke.cbrm.business.CbrmService;
import dke.cbrm.business.ModificationOperationService;
import dke.cbrm.persistence.model.Context;
import dke.cbrm.persistence.model.Parameter;
import lombok.RequiredArgsConstructor;

/**
 * @author ahauer
 * 
 *         Class responsible for collecting input necessary for
 *         composed modification operation 'split context'
 *
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ContextSplitWindow extends Window {

    private static final long serialVersionUID = -6760702627088847240L;

    private final CbrmService cbrmService;

    private final ModificationOperationService modOpsService;

    private final TreeGridCommons treeGridCommons;

    private Map<Parameter, Parameter> firstContextSelectedParametersMap,
	    secondContextSelectedParametersMap;

    private Button splitContextButton, abortSplitContextButton, addParameter,
	    addParameterValue;

    private HorizontalLayout hTopButtonLayout, hBottomButtonLayout;

    private VerticalLayout mainVerticalLayout, splitVerticalLayout,
	    splitGridFirstContextVerticalLayout,
	    splitGridSecondContextVerticalLayout;;

    private Context initialContext;

    private int parametersCount;

    public void setUp(Context initialContext) {
	this.initialContext = initialContext;

	hTopButtonLayout = new HorizontalLayout();
	hBottomButtonLayout = new HorizontalLayout();

	abortSplitContextButton = new Button("Abort");
	abortSplitContextButton.addClickListener(new ClickListener() {

	    private static final long serialVersionUID = 1L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		close();
	    }

	});

	splitContextButton = new Button("Split");
	splitContextButton.addClickListener(new ClickListener() {

	    private static final long serialVersionUID = -1722387077770270359L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		if (firstContextSelectedParametersMap.size() == parametersCount
			&& secondContextSelectedParametersMap
				.size() == parametersCount) {

		    // StringBuffer strBuffer = new StringBuffer();
		    //
		    // if (cbrmService.contextWithParametersExistsYet(
		    // initialContext.getInstantiatesContextModel(),
		    // firstContextSelectedParametersMap.values())) {
		    // strBuffer.append("Context: ");
		    // Iterator<Parameter> paramIter =
		    // firstContextSelectedParametersMap.values()
		    // .iterator();
		    // while (paramIter.hasNext()) {
		    // strBuffer.append(paramIter.next().getValue() + " ");
		    // }
		    // }
		    //
		    // if (cbrmService.contextWithParametersExistsYet(
		    // initialContext.getInstantiatesContextModel(),
		    // secondContextSelectedParametersMap.values())) {
		    // strBuffer.append("Context: ");
		    // Iterator<Parameter> paramIter =
		    // secondContextSelectedParametersMap.values()
		    // .iterator();
		    // while (paramIter.hasNext()) {
		    // strBuffer.append(paramIter.next().getValue() + " ");
		    // }
		    // }
		    //
		    // if (strBuffer.length() > 0) {
		    // Notification.show("Context with parameter values allready
		    // exists: ",
		    // strBuffer.toString(),
		    // Notification.Type.ERROR_MESSAGE);
		    // } else {
		    cbrmService.createContext(
			    firstContextSelectedParametersMap.values(),
			    initialContext);
		    cbrmService.createContext(
			    secondContextSelectedParametersMap.values(),
			    initialContext);
		    ViewUpdater.updateViews();
		    close();
		} else {
		    Notification.show("Insufficient Information: ",
			    "Please select a paramater value for every Parameter and Context",
			    Notification.Type.ERROR_MESSAGE);
		}
	    }

	});

	addParameter = new Button("Add parameter");
	addParameter.addClickListener(new ClickListener() {

	    private static final long serialVersionUID = -3893146783335973601L;

	    @Override
	    public void buttonClick(ClickEvent event) {

	    }

	});

	addParameterValue = new Button("Add parameter value");
	addParameterValue.addClickListener(new ClickListener() {

	    private static final long serialVersionUID = 3217660695100343525L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		// TODO Auto-generated method stub

	    }

	});

	hTopButtonLayout.addComponent(abortSplitContextButton);
	hTopButtonLayout.addComponent(addParameter);
	hTopButtonLayout.addComponent(addParameterValue);
	hTopButtonLayout.addComponent(splitContextButton);

	hBottomButtonLayout.addComponent(abortSplitContextButton);
	hBottomButtonLayout.addComponent(addParameter);
	hBottomButtonLayout.addComponent(addParameterValue);
	hBottomButtonLayout.addComponent(splitContextButton);

	splitVerticalLayout = new VerticalLayout();
	splitVerticalLayout.addComponent(addParameterGridToLayout());

	mainVerticalLayout = new VerticalLayout();
	mainVerticalLayout.addComponent(hTopButtonLayout);
	mainVerticalLayout.addComponent(splitVerticalLayout);
	mainVerticalLayout.addComponent(hBottomButtonLayout);

	this.setContent(mainVerticalLayout);
    }

    @SuppressWarnings("unchecked")
    private HorizontalLayout addParameterGridToLayout() {
	firstContextSelectedParametersMap = new HashMap<Parameter, Parameter>();
	secondContextSelectedParametersMap =
		new HashMap<Parameter, Parameter>();

	splitGridFirstContextVerticalLayout =
		new VerticalLayout(new Label("First Context"));
	splitGridFirstContextVerticalLayout.setResponsive(true);
	splitGridSecondContextVerticalLayout =
		new VerticalLayout(new Label("Second Context"));
	splitGridSecondContextVerticalLayout.setResponsive(true);

	Map<Parameter, Map<Parameter, Set<Parameter>>> childrenParametersMap =
		cbrmService.getChildrenParametersForContext(initialContext);
	parametersCount = childrenParametersMap.size();

	for (Entry<Parameter, Map<Parameter, Set<Parameter>>> params : childrenParametersMap
		.entrySet()) {
	    Parameter rootParam = params.getKey();
	    for (Entry<Parameter, Set<Parameter>> paramValues : params
		    .getValue().entrySet()) {

		TreeGrid<Parameter> parameterFirstContextGrid =
			new TreeGrid<Parameter>(Parameter.class);
		parameterFirstContextGrid.setResponsive(true);
		parameterFirstContextGrid
			.setCaption(params.getKey().getValue());
		parameterFirstContextGrid.setResponsive(true);
		parameterFirstContextGrid
			.setColumns(CbrmUI.VALUE_PROPERTY_OF_PARENT_CHILD);
		parameterFirstContextGrid
			.setSelectionMode(SelectionMode.SINGLE);
		parameterFirstContextGrid.addSelectionListener(
			new SelectionListener<Parameter>() {

			    private static final long serialVersionUID =
				    5630693401445786817L;

			    @Override
			    public void selectionChange(
				    SelectionEvent<Parameter> event) {
				Optional<Parameter> opt =
					event.getFirstSelectedItem();
				if (opt.isPresent()) {
				    Parameter param = (Parameter) opt.get();
				    firstContextSelectedParametersMap
					    .put(rootParam, param);
				}
			    }
			});

		TreeDataProvider<Parameter> parameterFirstContextGridDataProvider =
			(TreeDataProvider<Parameter>) parameterFirstContextGrid
				.getDataProvider();
		treeGridCommons.addItemsToTreeGrid(
			parameterFirstContextGridDataProvider.getTreeData(),
			paramValues.getKey(), parameterFirstContextGrid, true);

		splitGridFirstContextVerticalLayout
			.addComponent(parameterFirstContextGrid);

		TreeGrid<Parameter> parameterSecondContextGrid =
			new TreeGrid<Parameter>(Parameter.class);
		parameterSecondContextGrid.setResponsive(true);
		parameterSecondContextGrid
			.setCaption(params.getKey().getValue());
		parameterSecondContextGrid
			.setColumns(CbrmUI.VALUE_PROPERTY_OF_PARENT_CHILD);
		parameterSecondContextGrid
			.setSelectionMode(SelectionMode.SINGLE);
		parameterSecondContextGrid.addSelectionListener(
			new SelectionListener<Parameter>() {

			    private static final long serialVersionUID =
				    -7337561408981131968L;

			    @Override
			    public void selectionChange(
				    SelectionEvent<Parameter> event) {
				Optional<Parameter> opt =
					event.getFirstSelectedItem();
				if (opt.isPresent()) {
				    Parameter param = (Parameter) opt.get();
				    secondContextSelectedParametersMap
					    .put(rootParam, param);
				}
			    }
			});

		TreeDataProvider<Parameter> parameterSecondContextGridDataProvider =
			(TreeDataProvider<Parameter>) parameterSecondContextGrid
				.getDataProvider();
		treeGridCommons.addItemsToTreeGrid(
			parameterSecondContextGridDataProvider.getTreeData(),
			paramValues.getKey(), parameterSecondContextGrid, true);

		splitGridSecondContextVerticalLayout
			.addComponent(parameterSecondContextGrid);
	    }
	}

	return new HorizontalLayout(splitGridFirstContextVerticalLayout,
		splitGridSecondContextVerticalLayout);
    }

}
