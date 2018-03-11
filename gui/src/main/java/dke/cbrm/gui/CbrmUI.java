package dke.cbrm.gui;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.collect.Sets;
import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.event.selection.SelectionListener;
import com.vaadin.event.selection.SingleSelectionEvent;
import com.vaadin.event.selection.SingleSelectionListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TreeGrid;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

import dke.cbrm.CbrmConstants;
import dke.cbrm.business.CbrmService;
import dke.cbrm.business.PermissionService;
import dke.cbrm.gui.Broadcaster.BroadcastListener;
import dke.cbrm.persistence.model.Context;
import dke.cbrm.persistence.model.ContextModel;
import dke.cbrm.persistence.model.DetParamValue;
import dke.cbrm.persistence.model.ModificationOperation;
import dke.cbrm.persistence.model.ModificationOperationType;
import dke.cbrm.persistence.model.Parameter;
import dke.cbrm.persistence.model.ParentChildRelation;
import dke.cbrm.persistence.model.Rule;
import dke.cbrm.persistence.model.User;
import dke.cbrm.persistence.parser.FloraFileParser;
import lombok.RequiredArgsConstructor;

/**
 * This UI is the application entry point. A UI may either represent a
 * browser window (or tab) or some part of an HTML page where a Vaadin
 * application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This
 * method is intended to be overridden to add component to the user
 * interface and initialize non-component functionality.
 */
@SpringUI(path = "/main")
@Theme("guitheme")
@Push(PushMode.MANUAL)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CbrmUI extends UI implements BroadcastListener {

    private static final long serialVersionUID = 811817939183044951L;

    @Value("${cbrm.workspace}")
    private String workspace;

    private static final String VALUE_PROPERTY_OF_PARENT_CHILD = "value";

    private final CbrmService cbrmService;

    private final PermissionService permissionService;

    private final UploadWindow uploadWindow;

    @SuppressWarnings("rawtypes")
    private TreeGrid<ParentChildRelation> contextTreeGrid, parameterTreeGrid;

    private Button logOutButton, addDetParamValueButton,
	    addNewRuleToContextButton, saveNewRuleToContextButton,
	    saveAddDetParamValueButton, abortAddDetParamValueButton,
	    abortAddNewRuleToContextButton, startUploadButton,
	    addToAllowedUsersButton, deleteFromAllowedUsersButton;

    private TextArea addNewDetParamValueTextArea, addNewRuleToContextTextArea;

    private ComboBox<ContextModel> contextModelMenu;

    private Grid<User> allowedUsersTable, notAllowedUsersTable;

    private Context contextSelected;

    private ContextModel currentContextModel;

    private Parameter parameterSelected;

    private List<ModificationOperation> modificationOperationsInterestedIn;

    private VerticalLayout mainVerticalLayout, vParameterLayout,
	    vContextMgmtLayout, vContextLayout;

    private HorizontalLayout hMainContextLayout;

    private TabSheet tabSheet;

    private List<Component> detParamValuesToEdit, rulesToEdit;

    private boolean isRepoAdmin, isRuleDev, allowedToEditContextSelected;

    private String loggedInUser;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected void init(VaadinRequest request) {

	/**
	 * Register this UI session for push notifications
	 * from @link{BroadCaster}
	 */
	Broadcaster.register(this);

	determineUserSessionRoles();

	rulesToEdit = new ArrayList<Component>();
	detParamValuesToEdit = new ArrayList<Component>();

	tabSheet = new TabSheet();
	tabSheet.setSizeFull();
	mainVerticalLayout = new VerticalLayout();

	Label userLabel = new Label("Logged in: ".concat(loggedInUser));
	HorizontalLayout hTopLayout = new HorizontalLayout();
	hTopLayout.addComponent(userLabel);

	logOutButton = new Button("Log out", new ClickListener() {

	    private static final long serialVersionUID = 4904600428281019027L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		logout();
	    }
	});
	hTopLayout.addComponent(logOutButton);

	contextModelMenu = new ComboBox();
	contextModelMenu.setTextInputAllowed(false);
	contextModelMenu.setItems(
		Lists.newArrayList(cbrmService.getAllContextModelsAvailable()));

	contextModelMenu.addSelectionListener(
		new SingleSelectionListener<ContextModel>() {

		    private static final long serialVersionUID =
			    7174339486138093561L;

		    @Override
		    public void selectionChange(
			    SingleSelectionEvent<ContextModel> event) {
			currentContextModel = event.getValue();
			if (currentContextModel != null) {
			    createContextTreeGrid();
			    createParameterTreeGrid();
			} else {
			    tabSheet.removeComponent(hMainContextLayout);
			    tabSheet.removeComponent(vParameterLayout);
			}
		    }
		});

	hTopLayout.addComponent(contextModelMenu);

	if (isRepoAdmin) {
	    startUploadButton = new Button("Upload new Context Model");
	    startUploadButton.addClickListener(new ClickListener() {

		private static final long serialVersionUID =
			5087955774962515642L;

		@Override
		public void buttonClick(ClickEvent event) {
		    uploadWindow.setUp();
		    uploadWindow.center();
		    addWindow(uploadWindow);
		}
	    });
	    hTopLayout.addComponent(startUploadButton);

	    uploadWindow.addCloseListener(new CloseListener() {

		private static final long serialVersionUID =
			397870872272669184L;

		@Override
		public void windowClose(CloseEvent e) {
		    contextModelMenu.setItems(Lists.newArrayList(
			    cbrmService.getAllContextModelsAvailable()));
		}
	    });
	}

	mainVerticalLayout.addComponent(hTopLayout);

	mainVerticalLayout.addComponent(tabSheet);

	setContent(mainVerticalLayout);
    }

    /**
     * Determines and saves @link{User} and @link{CbrmConstants.UserRoles}
     * to current User - UI session for further UI - composition decisions
     * (edit - Buttons etc.)
     */
    private void determineUserSessionRoles() {
	loggedInUser = SecurityContextHolder.getContext().getAuthentication()
		.getName();

	isRepoAdmin = permissionService.userHasRoleSpecified(loggedInUser,
		CbrmConstants.UserRoles.ROLE_REPO_ADMIN);
	isRuleDev = permissionService.userHasRoleSpecified(loggedInUser,
		CbrmConstants.UserRoles.ROLE_RULE_DEV);
    }

    /**
     * Adds the @link{TreeGrid} displaying @link{Context}s of currently
     * selected @link{ContextModel} and adds @link{TextArea}s and Save -
     * /Edit - @links{Button}s {@link Rule}-Objects belonging
     * to @link{Context} selected depending on user role logged in;
     * 
     * @{link CbrmConstants.UserRoles.ROLE_REPO_ADMIN} can grant
     *        WRITE_RULE_PRIVILEGE to Role RULE_DEV * @{link
     *        CbrmConstants.UserRoles.ROLE_RULE_DEV} can modify Rule if
     *        ROLE_REPO_ADIN granted this privilege before
     * 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void createContextTreeGrid() {
	this.contextTreeGrid = new TreeGrid<>(ParentChildRelation.class);

	TreeDataProvider<ParentChildRelation> contextDataProvider =
		(TreeDataProvider<ParentChildRelation>) contextTreeGrid
			.getDataProvider();

	Iterator<Context> ctxIter = cbrmService.getAllContextsAvailable();
	while (ctxIter.hasNext()) {
	    Context ctx = ctxIter.next();
	    if (ctx.getParent() == null) {
		addItemsToTreeGrid(contextDataProvider.getTreeData(), ctx);
	    }
	}
	contextDataProvider.refreshAll();

	contextTreeGrid.addSelectionListener(
		new SelectionListener<ParentChildRelation>() {

		    private static final long serialVersionUID =
			    4605665120072805744L;

		    @Override
		    public void selectionChange(
			    SelectionEvent<ParentChildRelation> event) {

			for (Component comp : rulesToEdit) {
			    vContextLayout.removeComponent(comp);
			}

			if (event.getFirstSelectedItem().isPresent()) {
			    contextSelected = (Context) event
				    .getFirstSelectedItem().get();
			}

			allowedToEditContextSelected =
				isRuleDev && permissionService
					.userAllowedToEditContextRules(
						contextSelected, loggedInUser);

			if (allowedToEditContextSelected) {
			    addNewRuleToContextButton.setVisible(true);
			    if (addNewRuleToContextTextArea.isVisible()) {
				addNewRuleToContextButton.setEnabled(false);
			    } else {
				addNewRuleToContextButton.setEnabled(true);
			    }
			} else {
			    addNewRuleToContextTextArea.setVisible(false);
			    saveNewRuleToContextButton.setVisible(false);
			    addNewRuleToContextButton.setVisible(false);
			}

			refreshRulesDisplayed(allowedToEditContextSelected);

			if (isRepoAdmin) {
			    refreshUserTables();
			}
		    }

		});

	contextTreeGrid.setColumns(VALUE_PROPERTY_OF_PARENT_CHILD);
	contextTreeGrid.getColumn(VALUE_PROPERTY_OF_PARENT_CHILD)
		.setCaption("Context Name");
	contextTreeGrid.setCaption("Context - Hierarchy");
	contextTreeGrid.setColumnReorderingAllowed(true);
	contextTreeGrid.setResponsive(true);
	contextTreeGrid.setSizeFull();

	// remove from tabSheet if context model already was loaded
	if (hMainContextLayout != null) {
	    tabSheet.removeComponent(hMainContextLayout);
	}

	hMainContextLayout = new HorizontalLayout();
	hMainContextLayout.setSizeFull();

	vContextLayout = new VerticalLayout();
	vContextLayout.addComponent(contextTreeGrid);

	saveNewRuleToContextButton = new Button("Save Rule");
	saveNewRuleToContextButton.setVisible(false);
	saveNewRuleToContextButton.addClickListener(new ClickListener() {

	    private static final long serialVersionUID = 1L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		Matcher matcher = FloraFileParser.RULE_PATTERN
			.matcher(addNewRuleToContextTextArea.getValue());

		if (matcher.find()) {
		    Rule rule = new Rule();
		    LocalDateTime now = LocalDateTime.now();
		    rule.setCreatedAt(now);
		    rule.setModifiedAt(now);

		    rule.setRuleName(matcher.find() ? matcher.group(1) : "");
		    rule.setRuleContent(addNewRuleToContextTextArea.getValue());
		    rule.setRelatesTo(contextSelected);

		    cbrmService.addOrUpdateRule(rule);

		    addNewRuleToContextButton.setEnabled(true);
		    saveNewRuleToContextButton.setVisible(false);
		    addNewRuleToContextTextArea.setVisible(false);

		    refreshRulesDisplayed(allowedToEditContextSelected);

		    ModificationOperation modOp = cbrmService.createModificationOperation(
			    addNewRuleToContextTextArea.getValue(), "",
			    ModificationOperationType.ADD_RULE,
			    contextSelected);
		    
		    Broadcaster.broadcast(modOp);
		} else {
		    Notification.show(
			    "No adequate Rule - Name! (e.g.: '@!{R102}') ",
			    Notification.Type.ERROR_MESSAGE);
		}
	    }
	});

	addNewRuleToContextTextArea = new TextArea();
	addNewRuleToContextTextArea.setSizeFull();
	vContextLayout.addComponent(addNewRuleToContextTextArea);
	addNewRuleToContextTextArea.setVisible(false);
	addNewRuleToContextTextArea
		.addValueChangeListener(new ValueChangeListener<String>() {

		    private static final long serialVersionUID = 1L;

		    @Override
		    public void valueChange(ValueChangeEvent<String> event) {
			saveNewRuleToContextButton.setVisible(true);
			addNewRuleToContextButton.setEnabled(false);
		    }
		});

	addNewRuleToContextButton =
		new Button("Add new Rule", VaadinIcons.PLUS);
	addNewRuleToContextButton.setVisible(false);

	abortAddNewRuleToContextButton = new Button("Abort");
	abortAddNewRuleToContextButton.setVisible(false);

	HorizontalLayout hButtonLayout = new HorizontalLayout();
	hButtonLayout.addComponent(addNewRuleToContextButton);
	hButtonLayout.addComponent(abortAddNewRuleToContextButton);
	hButtonLayout.addComponent(saveNewRuleToContextButton);

	vContextLayout.addComponent(hButtonLayout);
	addNewRuleToContextButton.addClickListener(new ClickListener() {

	    private static final long serialVersionUID = 389080120548279502L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		addNewRuleToContextTextArea.setVisible(true);
		addNewRuleToContextButton.setEnabled(false);
	    }
	});

	abortAddNewRuleToContextButton.addClickListener(new ClickListener() {

	    private static final long serialVersionUID = -1769990808571197274L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		addNewRuleToContextButton.setEnabled(true);
		addNewRuleToContextTextArea.setVisible(false);
		saveNewRuleToContextButton.setVisible(false);
	    }
	});

	hMainContextLayout.addComponent(vContextLayout);

	tabSheet.addTab(hMainContextLayout,
		"Context-Hierarchy with related Rules");

	if (isRepoAdmin) {
	    vContextMgmtLayout = new VerticalLayout();
	    hMainContextLayout.addComponent(vContextMgmtLayout);

	    allowedUsersTable = new Grid<User>(User.class);
	    allowedUsersTable.setCaption("Allowed Users");
	    removeColumnsFromUser(allowedUsersTable);
	    allowedUsersTable.setSizeFull();
	    vContextMgmtLayout.addComponent((Component) allowedUsersTable);

	    deleteFromAllowedUsersButton =
		    new Button("Delete Users", VaadinIcons.MINUS);
	    deleteFromAllowedUsersButton.addClickListener(new ClickListener() {

		private static final long serialVersionUID =
			5803835677170663489L;

		@Override
		public void buttonClick(ClickEvent event) {
		    if (!allowedUsersTable.getSelectedItems().isEmpty()) {
			for (User user : allowedUsersTable.getSelectedItems()) {
			    permissionService
				    .loadAllowedUsersOfContext(contextSelected);
			    contextSelected.getAllowedUsers().remove(user);
			}
			cbrmService.addOrUpdateContext(contextSelected);
			refreshUserTables();
		    }
		}

	    });
	    vContextMgmtLayout.addComponent(deleteFromAllowedUsersButton);

	    notAllowedUsersTable = new Grid<User>(User.class);
	    notAllowedUsersTable.setCaption("Not allowed Users");
	    removeColumnsFromUser(notAllowedUsersTable);
	    notAllowedUsersTable.setSizeFull();
	    vContextMgmtLayout.addComponent((Component) notAllowedUsersTable);

	    addToAllowedUsersButton = new Button("Add Users", VaadinIcons.PLUS);
	    addToAllowedUsersButton.addClickListener(new ClickListener() {

		private static final long serialVersionUID =
			6832371039240031280L;

		@Override
		public void buttonClick(ClickEvent event) {
		    if (!notAllowedUsersTable.getSelectedItems().isEmpty()) {
			for (User user : notAllowedUsersTable
				.getSelectedItems()) {
			    permissionService
				    .loadAllowedUsersOfContext(contextSelected);
			    contextSelected.getAllowedUsers().add(user);
			}
			cbrmService.addOrUpdateContext(contextSelected);
			refreshUserTables();
		    }
		}

	    });
	    vContextMgmtLayout.addComponent(addToAllowedUsersButton);
	}
    }

    /**
     * Refreshes the @link{Rule}s displayed depending on
     * the @link{Context} selected by UI - Session User
     * 
     * @param allowedToEdit
     *            indication for edit-/save Buttons to be displayed /
     *            added
     */
    private void refreshRulesDisplayed(boolean allowedToEdit) {
	Iterator<Rule> rules =
		cbrmService.getRulesByContextName(contextSelected.getValue(),
			currentContextModel.getName()).iterator();

	for (Component comp : rulesToEdit) {
	    vContextLayout.removeComponent(comp);
	}

	rulesToEdit = new ArrayList<Component>();

	while (rules.hasNext()) {
	    addRuleToTable(rules.next(), allowedToEdit);
	}
    }

    /**
     * Loads allowed ant not allowed Users depending on
     * selected @link{Context} into corresponding Grid
     */
    private void refreshUserTables() {
	Iterable<User> allowedUsersForContext =
		permissionService.getAllowedRuleDevsForContext(contextSelected);

	allowedUsersTable.setItems(Sets.newHashSet(allowedUsersForContext));

	List<User> notAllowedUsersForContext = (List<User>) permissionService
		.getNotAllowedUsersForContext(allowedUsersForContext);

	notAllowedUsersTable.setItems(notAllowedUsersForContext);
    }

    /**
     * Formats / removes Columns from Grid displaying {@link User}s
     * 
     * @param table
     *            the Grid to be formatted
     */
    private void removeColumnsFromUser(Grid<User> table) {
	table.removeColumn("id");
	table.removeColumn("roles");
	table.removeColumn("password");
	table.removeColumn("enabled");
	table.removeColumn("tokenExpired");
    }

    @SuppressWarnings("rawtypes")
    private void createParameterTreeGrid() {
	this.parameterTreeGrid = new TreeGrid<>(ParentChildRelation.class);

	@SuppressWarnings("unchecked")
	TreeDataProvider<ParentChildRelation> parameterDataProvider =
		(TreeDataProvider<ParentChildRelation>) parameterTreeGrid
			.getDataProvider();

	Iterator<Parameter> paramIter = cbrmService
		.getAllParametersOfContextModel(currentContextModel.getName());
	while (paramIter.hasNext()) {
	    Parameter param = paramIter.next();
	    if (param.getParent() == null) {
		addItemsToTreeGrid(parameterDataProvider.getTreeData(), param);
	    }
	}

	parameterDataProvider.refreshAll();
	parameterTreeGrid.addSelectionListener(
		new SelectionListener<ParentChildRelation>() {

		    private static final long serialVersionUID =
			    4605665120072805744L;

		    @Override
		    public void selectionChange(
			    SelectionEvent<ParentChildRelation> event) {

			for (Component comp : detParamValuesToEdit) {
			    vParameterLayout.removeComponent(comp);
			}

			if (event.getFirstSelectedItem().isPresent()) {
			    parameterSelected = (Parameter) event
				    .getFirstSelectedItem().get();
			}

			if (parameterSelected.getParent() == null) {

			    if (isRepoAdmin && !addNewDetParamValueTextArea
				    .isEnabled()) {
				addDetParamValueButton.setEnabled(true);
				addDetParamValueButton.setVisible(true);
			    }

			    Iterator<DetParamValue> detParamValues =
				    cbrmService.getDetParamValuesByParameterId(
					    parameterSelected.getParameterId());

			    detParamValuesToEdit = new ArrayList<Component>();

			    while (detParamValues.hasNext()) {
				DetParamValue detParamValue =
					detParamValues.next();
				addDetParamValueToTable(detParamValue,
					isRepoAdmin);
			    }
			} else {
			    saveAddDetParamValueButton.setVisible(false);
			    saveAddDetParamValueButton.setEnabled(false);
			    abortAddDetParamValueButton.setVisible(false);
			    abortAddDetParamValueButton.setEnabled(false);
			    addDetParamValueButton.setVisible(false);
			    addDetParamValueButton.setEnabled(false);
			    addNewDetParamValueTextArea.setVisible(false);
			    addNewDetParamValueTextArea.setEnabled(false);
			}
		    }
		});

	parameterTreeGrid.setColumns(VALUE_PROPERTY_OF_PARENT_CHILD);
	parameterTreeGrid.setColumnReorderingAllowed(true);
	parameterTreeGrid.setResponsive(true);
	parameterTreeGrid.setSizeFull();

	if (vParameterLayout != null) {
	    tabSheet.removeComponent(vParameterLayout);
	}

	vParameterLayout = new VerticalLayout();
	vParameterLayout.addComponent(parameterTreeGrid);
	tabSheet.addTab(vParameterLayout, "Parameter-Hierarchy");

	addDetParamValueButton =
		new Button("Add new DetParamValue-Method / Fact");
	addDetParamValueButton.setVisible(false);
	addDetParamValueButton.setEnabled(false);

	addDetParamValueButton.addClickListener(new ClickListener() {
	    private static final long serialVersionUID = 9199506609447060920L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		addNewDetParamValueTextArea.setEnabled(true);
		addNewDetParamValueTextArea.setVisible(true);

		addDetParamValueButton.setEnabled(false);
		addDetParamValueButton.setVisible(false);

		abortAddDetParamValueButton.setVisible(true);
		abortAddDetParamValueButton.setEnabled(true);

		ModificationOperation modOp = new ModificationOperation();
		modOp.setModificationOperationType(
			ModificationOperationType.MODIFY_PARAMETER);

		Broadcaster.broadcast(modOp);
	    }
	});

	abortAddDetParamValueButton = new Button("Abort");
	abortAddDetParamValueButton.setEnabled(false);
	abortAddDetParamValueButton.setVisible(false);
	abortAddDetParamValueButton.addClickListener(new ClickListener() {
	    private static final long serialVersionUID = 8941737106635368177L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		addNewDetParamValueTextArea.setVisible(false);
		addNewDetParamValueTextArea.setEnabled(false);
		addDetParamValueButton.setVisible(true);
		addDetParamValueButton.setEnabled(true);
		saveAddDetParamValueButton.setEnabled(false);
		saveAddDetParamValueButton.setVisible(false);
		abortAddDetParamValueButton.setEnabled(false);
		abortAddDetParamValueButton.setVisible(false);
	    }
	});

	saveAddDetParamValueButton = new Button("Save");
	saveAddDetParamValueButton.setEnabled(false);
	saveAddDetParamValueButton.setVisible(false);

	HorizontalLayout hAddDetParamValueButtonLayout = new HorizontalLayout();
	hAddDetParamValueButtonLayout.addComponent(addDetParamValueButton);
	hAddDetParamValueButtonLayout.addComponent(abortAddDetParamValueButton);
	hAddDetParamValueButtonLayout.addComponent(saveAddDetParamValueButton);

	vParameterLayout.addComponent(hAddDetParamValueButtonLayout);
	addNewDetParamValueTextArea =
		new TextArea("New detParamValue - Method / Fact");
	addNewDetParamValueTextArea.setSizeFull();

	addNewDetParamValueTextArea
		.addValueChangeListener(new ValueChangeListener<String>() {
		    private static final long serialVersionUID =
			    6040594917134354557L;

		    @Override
		    public void valueChange(ValueChangeEvent<String> event) {
			saveAddDetParamValueButton.setEnabled(true);
			saveAddDetParamValueButton.setVisible(true);
		    }
		});
	addNewDetParamValueTextArea.setVisible(false);
	addNewDetParamValueTextArea.setEnabled(false);

	vParameterLayout.addComponent(addNewDetParamValueTextArea);
    }

    private void addDetParamValueToTable(DetParamValue detParamValue,
	    boolean allowedToEdit) {

	TextArea detParamValueTextArea = new TextArea();
	final Button saveChangesButton = new Button("Save Changes"), editButton;
	saveChangesButton.setVisible(false);
	saveChangesButton.addClickListener(new ClickListener() {
	    private static final long serialVersionUID = 1777006486840750950L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		detParamValue.setContent(detParamValueTextArea.getValue());

		cbrmService.addOrUpdateDetParamValue(detParamValue);
	    }
	});

	detParamValueTextArea.setValue(detParamValue.getContent());
	detParamValueTextArea.setEnabled(false);
	detParamValueTextArea.setSizeFull();

	detParamValueTextArea
		.addValueChangeListener(new ValueChangeListener<String>() {

		    private static final long serialVersionUID =
			    8044374648461120974L;

		    @Override
		    public void valueChange(ValueChangeEvent<String> event) {

			event.getValue();

			if (allowedToEdit) {
			    saveChangesButton.setVisible(true);
			    saveChangesButton.setEnabled(true);
			}
		    }
		});
	vParameterLayout.addComponent(detParamValueTextArea);
	detParamValuesToEdit.add(detParamValueTextArea);

	if (allowedToEdit) {
	    editButton = new Button("Edit");
	    editButton.setEnabled(true);
	    editButton.addClickListener(new ClickListener() {

		private static final long serialVersionUID =
			3629963246951678512L;

		@Override
		public void buttonClick(ClickEvent event) {
		    detParamValueTextArea.setEnabled(true);
		}

	    });
	    HorizontalLayout hLayout = new HorizontalLayout();
	    hLayout.addComponent(editButton);
	    hLayout.addComponent(saveChangesButton);
	    vParameterLayout.addComponent(hLayout);
	    detParamValuesToEdit.add(hLayout);
	}
    }

    private void addRuleToTable(Rule rule, boolean allowedToEdit) {
	TextArea rulesTextArea = new TextArea();
	final Button saveChangesButton = new Button("Save Changes"), editButton,
		deleteButton = new Button("Delete Rule");

	rulesTextArea.setValue(rule.getRuleContent());
	rulesTextArea.setEnabled(false);
	rulesTextArea.setSizeFull();
	rulesTextArea.addValueChangeListener(new ValueChangeListener<String>() {

	    private static final long serialVersionUID = 8044374648461120974L;

	    @Override
	    public void valueChange(ValueChangeEvent<String> event) {

		event.getValue();

		if (allowedToEdit) {
		    saveChangesButton.setVisible(true);
		    saveChangesButton.setEnabled(true);
		}
	    }
	});
	vContextLayout.addComponent(rulesTextArea);
	rulesToEdit.add(rulesTextArea);

	if (allowedToEdit) {
	    editButton = new Button("Edit");
	    editButton.setEnabled(true);
	    editButton.addClickListener(new ClickListener() {

		private static final long serialVersionUID =
			3629963246951678512L;

		@Override
		public void buttonClick(ClickEvent event) {
		    rulesTextArea.setEnabled(true);
		}

	    });

	    saveChangesButton.setEnabled(false);
	    saveChangesButton.setVisible(false);
	    saveChangesButton.addClickListener(new ClickListener() {

		private static final long serialVersionUID =
			270537553897472213L;

		@Override
		public void buttonClick(ClickEvent event) {
		    saveChangesButton.setEnabled(false);
		    saveChangesButton.setVisible(false);
		    rulesTextArea.setEnabled(false);

		    rule.setRuleContent(rulesTextArea.getValue());

		    cbrmService.addOrUpdateRule(rule);
		}
	    });

	    deleteButton.addClickListener(new ClickListener() {

		private static final long serialVersionUID = 1L;

		@Override
		public void buttonClick(ClickEvent event) {
		    cbrmService.deleteRule(contextSelected, rule);
		    refreshRulesDisplayed(allowedToEdit);
		    ModificationOperation modOp = new ModificationOperation();
		    modOp.setModificationOperationType(
			    ModificationOperationType.DELETE_RULE);
		    Broadcaster.broadcast(modOp);
		}
	    });

	    HorizontalLayout hLayout = new HorizontalLayout();
	    hLayout.addComponent(editButton);
	    hLayout.addComponent(deleteButton);
	    hLayout.addComponent(saveChangesButton);
	    vContextLayout.addComponent(hLayout);
	    rulesToEdit.add(hLayout);
	}
    }

    /**
     * Recursively iterates with the first given @{link
     * ParentChildRelation}-parent-Object through resulting Object-Tree
     * and adds them to @{link TreeData}
     * 
     * @param data
     *            the data to be displayed
     * @param parent
     *            the Context to be iterated through
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void addItemsToTreeGrid(TreeData<ParentChildRelation> data,
	    ParentChildRelation parent) {

	if (parent.getParent() == null) {
	    data.addItem(null, parent);
	}

	if (parent instanceof Context) {
	    parent = cbrmService.getChildren((Context) parent);
	} else {
	    parent = cbrmService.getChildren((Parameter) parent);
	}

	if (!parent.getChildren().isEmpty()) {
	    data.addItems(parent, parent.getChildren());
	    for (ParentChildRelation child : (Set<ParentChildRelation>) parent
		    .getChildren()) {
		addItemsToTreeGrid(data, child);
	    }
	}

	/** expand the parent node, to finally render a fully expanded tree */
	if (parent instanceof Context) {
	    contextTreeGrid.expand(parent);
	} else {
	    parameterTreeGrid.expand(parent);
	}
    }

    /**
     * Invalidates authenticated Session-Context for logged in User
     */
    public void logout() {
	SecurityContextHolder.clearContext();
	Optional.ofNullable(VaadinSession.getCurrent())
		.ifPresent(session -> session.close());
	getPage().setLocation("/login");
    }

    @Override
    public void receiveBroadcast(ModificationOperation message) {
	access(new Runnable() {
	    @Override
	    public void run() {
		System.out.println(loggedInUser + " received broadcast");
		Notification n = new Notification("Modification Operation",
			message.getModificationOperationType().name(),
			Type.TRAY_NOTIFICATION);
		n.setDelayMsec(-1);
		n.show(getPage());
	    }
	});
    }

    /**
     * Instances of this class have to be unregistered from
     * 
     * @link{Broadcaster} when the UI - session expires
     */
    @Override
    public void detach() {
	Broadcaster.unregister(this);
	super.detach();
    }
}
