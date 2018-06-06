package dke.cbrm.gui;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

import org.atmosphere.config.service.Get;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.collect.Sets;
import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.event.selection.SelectionListener;
import com.vaadin.event.selection.SingleSelectionEvent;
import com.vaadin.event.selection.SingleSelectionListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
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
import dke.cbrm.CbrmUtils;
import dke.cbrm.business.CbrmService;
import dke.cbrm.business.PermissionService;
import dke.cbrm.business.statemachine.CbrmStateService;
import dke.cbrm.gui.Broadcaster.BroadcastListener;
import dke.cbrm.gui.dto.DetParamValueComponent;
import dke.cbrm.gui.dto.RuleComponent;
import dke.cbrm.persistence.model.Context;
import dke.cbrm.persistence.model.ContextModel;
import dke.cbrm.persistence.model.DetParamValue;
import dke.cbrm.persistence.model.ModificationOperation;
import dke.cbrm.persistence.model.ModificationOperationType;
import dke.cbrm.persistence.model.Parameter;
import dke.cbrm.persistence.model.Rule;
import dke.cbrm.persistence.model.User;
import dke.cbrm.persistence.parser.FloraFileParser;
import lombok.Getter;
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
public class CbrmUI extends UI implements BroadcastListener, CbrmView {

    private static final long serialVersionUID = 811817939183044951L;

    @Value("${cbrm.workspace}")
    private String workspace;

    private static final String PAGE_TITLE = "CBRM - GUI";

    public static final String VALUE_PROPERTY_OF_PARENT_CHILD = "value";

    public static final String PARAMETER_PROPERTY_OF_PARENT_CHILD =
	    "constitutingParameterValues";

    private final CbrmStateService cbrmStateService;

    private final CbrmService cbrmService;

    private final PermissionService permissionService;

    private final TreeGridCommons treeGridCommons;

    private final UploadWindow uploadWindow;

    private final ContextSplitWindow contextSplitWindow;

    private final UserManagementTab managementTab;

    private final ModOpsUI modOpsUi;

    private final SendMessageComponent sendMessageComponent;

    private TreeGrid<Context> contextTreeGrid;

    private TreeGrid<Parameter> parameterTreeGrid;

    private TreeDataProvider<Context> contextDataProvider;

    private TreeDataProvider<Parameter> parameterDataProvider;

    private Button logOutButton, addDetParamValueButton,
	    addNewRuleToContextButton, saveNewRuleToContextButton,
	    saveAddDetParamValueButton, abortAddDetParamValueButton,
	    abortAddNewRuleToContextButton, startUploadButton,
	    addToAllowedUsersButton, deleteFromAllowedUsersButton,
	    splitContextButton;

    private TextArea addNewDetParamValueTextArea, addNewRuleToContextTextArea;

    private ComboBox<ContextModel> contextModelMenu;

    private Grid<User> allowedUsersTable, notAllowedUsersTable;

    @Getter
    private Context contextSelected;

    private ContextModel currentContextModel;

    private Parameter parameterSelected;

    private VerticalLayout mainVerticalLayout, vParameterLayout,
	    vContextMgmtLayout, vContextLayout;

    private HorizontalLayout hMainContextLayout, hMainModOpLayout,
	    hUserButtonLayout, hRulesButtonLayout, hModOpsButtonLayout;

    private TabSheet tabSheet;

    private List<RuleComponent> rulesToEdit;

    private List<DetParamValueComponent> detParamValuesToEdit;

    private boolean isRepoAdmin, isRuleDev, isUser, isDomainExpert,
	    allowedToEditContextSelected;

    private Authentication loggedInUser;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected void init(VaadinRequest request) {

	/**
	 * Register this UI session for push notifications
	 * from @link{BroadCaster}
	 */
	Broadcaster.register(this);
	ViewUpdater.registerView(this);

	determineUserSessionRoles();

	rulesToEdit = new ArrayList<RuleComponent>();
	detParamValuesToEdit = new ArrayList<DetParamValueComponent>();

	tabSheet = new TabSheet();
	tabSheet.setSizeFull();
	mainVerticalLayout = new VerticalLayout();

	Label userLabel =
		new Label("Logged in: ".concat(loggedInUser.getName()));
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
			    createModOpGrid();
			} else {
			    tabSheet.removeComponent(hMainContextLayout);
			    tabSheet.removeComponent(vParameterLayout);
			    tabSheet.removeComponent(hMainModOpLayout);
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

	    refreshUserTables();
	    createUserRoleManagement();
	}

	mainVerticalLayout.addComponent(hTopLayout);
	mainVerticalLayout.addComponent(tabSheet);

	setContent(mainVerticalLayout);
	Page.getCurrent().setTitle(PAGE_TITLE);
    }

    /**
     * Determines and saves @link{User} and @link{CbrmConstants.UserRoles}
     * to current User - UI session for further UI - composition decisions
     * (edit - Buttons etc.)
     */
    private void determineUserSessionRoles() {
	loggedInUser = SecurityContextHolder.getContext().getAuthentication();

	isRepoAdmin =
		permissionService.userHasRoleSpecified(loggedInUser.getName(),
			CbrmConstants.UserRoles.ROLE_REPO_ADMIN);

	isUser = permissionService.userHasRoleSpecified(loggedInUser.getName(),
		CbrmConstants.UserRoles.ROLE_USER);

	isDomainExpert =
		permissionService.userHasRoleSpecified(loggedInUser.getName(),
			CbrmConstants.UserRoles.ROLE_DOMAIN_EXPERT);

	isRuleDev = permissionService.userHasRoleSpecified(
		loggedInUser.getName(), CbrmConstants.UserRoles.ROLE_RULE_DEV);
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
    private void createContextTreeGrid() {
	splitContextButton = new Button("Split context");
	splitContextButton.addClickListener(new Button.ClickListener() {

	    private static final long serialVersionUID = 1174302638872298509L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		if (contextSelected != null) {

		    contextSplitWindow.setUp(contextSelected);
		    contextSplitWindow.center();
		    addWindow(contextSplitWindow);
		    // first get administrator input for new contexts (maybe
		    // also new parameter values needed)
		    // cbrmService.splitContext(contextSelected,
		    // firstNewContext, secondNewContext);
		} else {
		    Notification.show("No context selected: ",
			    "split only possible with specified context",
			    Notification.Type.ERROR_MESSAGE);
		}
	    }
	});
	splitContextButton.setEnabled(false);
	splitContextButton.setVisible(isRepoAdmin);

	refreshContextsDisplayed();

	contextTreeGrid.addSelectionListener(new SelectionListener<Context>() {

	    private static final long serialVersionUID = 4605665120072805744L;

	    @Override
	    public void selectionChange(SelectionEvent<Context> event) {

		for (RuleComponent comp : rulesToEdit) {
		    vContextLayout.removeComponent(comp.getComponent());
		}

		if (event.getFirstSelectedItem().isPresent()) {
		    contextSelected =
			    (Context) event.getFirstSelectedItem().get();
		}

		allowedToEditContextSelected = isRuleDev
			&& permissionService.userAllowedToEditContextRules(
				contextSelected, loggedInUser.getName());

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

		splitContextButton
			.setEnabled(!cbrmStateService.stateMachineIsRunning());

		refreshRulesDisplayed(allowedToEditContextSelected);
	    }

	});

	contextTreeGrid.setColumns(PARAMETER_PROPERTY_OF_PARENT_CHILD);
	contextTreeGrid.getColumn(PARAMETER_PROPERTY_OF_PARENT_CHILD)
		.setCaption("Paramaters determining Context");
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
		    addNewRuleToContextButton.setEnabled(true);
		    saveNewRuleToContextButton.setVisible(false);
		    addNewRuleToContextTextArea.setVisible(false);

		    refreshRulesDisplayed(allowedToEditContextSelected);

		    LocalDateTime now = LocalDateTime.now();
		    Rule rule = new Rule();
		    rule.setCreatedAt(now);
		    rule.setModifiedAt(now);
		    rule.setRelatesTo(contextSelected);
		    rule.setRuleName(matcher.group(1));
		    rule.setRuleContent(matcher.group());

		    ModificationOperation modOp =
			    cbrmService.createModificationOperation(
				    ModificationOperationType.ADD_RULE, null,
				    contextSelected, null, null);

		    Broadcaster.broadcast(modOp,
			    cbrmService.getAffectedUsersFromModOp(modOp));
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

	hRulesButtonLayout = new HorizontalLayout();
	hRulesButtonLayout.addComponent(addNewRuleToContextButton);
	hRulesButtonLayout.addComponent(abortAddNewRuleToContextButton);
	hRulesButtonLayout.addComponent(saveNewRuleToContextButton);

	hModOpsButtonLayout = new HorizontalLayout();

	if (isRepoAdmin) {
	    hModOpsButtonLayout.addComponent(splitContextButton);
	}

	if (isUser || isDomainExpert) {
	    sendMessageComponent.initialize(this);
	    hModOpsButtonLayout.addComponent(sendMessageComponent);
	}

	vContextLayout.addComponent(hRulesButtonLayout);
	vContextLayout.addComponent(hModOpsButtonLayout);

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
	    createContextUsersManagement();
	}
    }

    private void createUserRoleManagement() {
	managementTab.instantiate();
	tabSheet.addTab(managementTab, "User Role Management");
    }

    private void createContextUsersManagement() {
	vContextMgmtLayout = new VerticalLayout();
	hMainContextLayout.addComponent(vContextMgmtLayout);

	allowedUsersTable = new Grid<User>(User.class);
	allowedUsersTable.setCaption("Allowed Users");
	CbrmUtils.removeColumnsFromUser(allowedUsersTable);
	allowedUsersTable.setSizeFull();
	vContextMgmtLayout.addComponent((Component) allowedUsersTable);

	deleteFromAllowedUsersButton =
		new Button("Delete Users", VaadinIcons.MINUS);
	deleteFromAllowedUsersButton.addClickListener(new ClickListener() {

	    private static final long serialVersionUID = 5803835677170663489L;

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

	addToAllowedUsersButton = new Button("Add Users", VaadinIcons.PLUS);
	addToAllowedUsersButton.addClickListener(new ClickListener() {

	    private static final long serialVersionUID = 6832371039240031280L;

	    @Get

	    @Override
	    public void buttonClick(ClickEvent event) {
		if (!notAllowedUsersTable.getSelectedItems().isEmpty()) {
		    for (User user : notAllowedUsersTable.getSelectedItems()) {
			permissionService
				.loadAllowedUsersOfContext(contextSelected);
			contextSelected.getAllowedUsers().add(user);
		    }
		    cbrmService.addOrUpdateContext(contextSelected);
		    refreshUserTables();
		}
	    }

	});

	hUserButtonLayout = new HorizontalLayout(addToAllowedUsersButton,
		deleteFromAllowedUsersButton);
	vContextMgmtLayout.addComponent(hUserButtonLayout);

	notAllowedUsersTable = new Grid<User>(User.class);
	notAllowedUsersTable.setCaption("Not allowed Users");
	CbrmUtils.removeColumnsFromUser(notAllowedUsersTable);
	notAllowedUsersTable.setSizeFull();
	vContextMgmtLayout.addComponent((Component) notAllowedUsersTable);
    }

    private void createModOpGrid() {
	modOpsUi.initialize(loggedInUser);
	hMainModOpLayout = new HorizontalLayout(modOpsUi);
	hMainModOpLayout.setSizeFull();
	tabSheet.addTab(hMainModOpLayout, "Open Modification Operations");
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

	for (RuleComponent comp : rulesToEdit) {
	    vContextLayout.removeComponent(comp.getComponent());
	}

	rulesToEdit = new ArrayList<RuleComponent>();

	while (rules.hasNext()) {
	    addRuleToTable(rules.next(), allowedToEdit);
	}
    }

    @SuppressWarnings({ "unchecked" })
    private void refreshContextsDisplayed() {
	this.contextTreeGrid = new TreeGrid<>(Context.class);

	contextDataProvider =
		(TreeDataProvider<Context>) contextTreeGrid.getDataProvider();
	Iterator<Context> ctxIter =
		cbrmService.getAllContextsOfContextModel(currentContextModel);

	while (ctxIter.hasNext()) {
	    Context ctx = ctxIter.next();
	    if (ctx.getParent() == null) {
		treeGridCommons.addItemsToTreeGrid(
			contextDataProvider.getTreeData(), ctx, contextTreeGrid,
			true);
	    }
	}
	contextDataProvider.refreshAll();
    }

    /**
     * Loads allowed ant not allowed Users depending on
     * selected @link{Context} into corresponding Grid
     */
    private void refreshUserTables() {
	if (contextSelected != null) {
	    Iterable<User> allowedUsersForContext = permissionService
		    .getAllowedRuleDevsForContext(contextSelected);

	    allowedUsersTable.setItems(Sets.newHashSet(allowedUsersForContext));

	    List<User> notAllowedUsersForContext =
		    (List<User>) permissionService.getNotAllowedUsersForContext(
			    allowedUsersForContext);

	    notAllowedUsersTable.setItems(notAllowedUsersForContext);
	}
    }

    @SuppressWarnings("unchecked")
    private void createParameterTreeGrid() {
	this.parameterTreeGrid = new TreeGrid<>(Parameter.class);

	parameterDataProvider = (TreeDataProvider<Parameter>) parameterTreeGrid
		.getDataProvider();

	Iterator<Parameter> paramIter =
		cbrmService.getAllParametersOfContextModel(currentContextModel);
	while (paramIter.hasNext()) {
	    Parameter param = paramIter.next();
	    if (param.getParent() == null) {
		treeGridCommons.addItemsToTreeGrid(
			parameterDataProvider.getTreeData(), param,
			parameterTreeGrid, true);
	    }
	}

	parameterDataProvider.refreshAll();
	parameterTreeGrid
		.addSelectionListener(new SelectionListener<Parameter>() {

		    private static final long serialVersionUID =
			    4605665120072805744L;

		    @Override
		    public void selectionChange(
			    SelectionEvent<Parameter> event) {
			for (DetParamValueComponent comp : detParamValuesToEdit) {
			    vParameterLayout
				    .removeComponent(comp.getComponent());
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

			    detParamValuesToEdit =
				    new ArrayList<DetParamValueComponent>();

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
	parameterTreeGrid.getColumn(VALUE_PROPERTY_OF_PARENT_CHILD)
		.setCaption("Parameter Value");
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

		LocalDateTime now = LocalDateTime.now();
		DetParamValue detParamValue = new DetParamValue();
		detParamValue.setCreatedAt(now);
		detParamValue.setModifiedAt(now);
		detParamValue
			.setContent(addNewDetParamValueTextArea.getValue());
		detParamValue.setParameter(parameterSelected);

		cbrmService.createModificationOperation(
			ModificationOperationType.MODIFY_PARAMETER,
			detParamValue, contextSelected, null, null);
		ModificationOperation modOp = new ModificationOperation();
		modOp.setModificationOperationType(
			ModificationOperationType.MODIFY_PARAMETER);

		Broadcaster.broadcast(modOp,
			cbrmService.getAffectedUsersFromModOp(modOp));
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
	detParamValuesToEdit.add(new DetParamValueComponent(detParamValue,
		detParamValueTextArea));

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
	    detParamValuesToEdit
		    .add(new DetParamValueComponent(detParamValue, hLayout));
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
	rulesToEdit.add(new RuleComponent(rule, rulesTextArea));

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
		    // cbrmService.deleteRule(contextSelected, rule);
		    // refreshRulesDisplayed(allowedToEdit);

		    cbrmService.createModificationOperation(
			    ModificationOperationType.DELETE_RULE, rule,
			    contextSelected, rule, null);

		    ModificationOperation modOp = new ModificationOperation();
		    modOp.setModificationOperationType(
			    ModificationOperationType.DELETE_RULE);
		    Broadcaster.broadcast(modOp,
			    cbrmService.getAffectedUsersFromModOp(modOp));
		}
	    });

	    HorizontalLayout hLayout = new HorizontalLayout();
	    hLayout.addComponent(editButton);
	    hLayout.addComponent(deleteButton);
	    hLayout.addComponent(saveChangesButton);
	    vContextLayout.addComponent(hLayout);
	    rulesToEdit.add(new RuleComponent(rule, hLayout));
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
    public void receiveBroadcast(ModificationOperation modOp) {
	access(new Runnable() {
	    @Override
	    public void run() {
		System.out.println(
			loggedInUser.getName() + " received broadcast");
		Notification n = new Notification("Modification Operation",
			modOp.getModificationOperationType().name()
				+ System.lineSeparator()
				+ "was created by User: "
				+ modOp.getCreatedBy().getUserName(),
			Type.TRAY_NOTIFICATION);
		n.setDelayMsec(-1);
		n.show(getPage());

		modOpsUi.addModificationOperation(modOp);
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

    public Authentication getLoggedInUser() {
	return loggedInUser;
    }

    @Override
    public void updateView() {
	this.refreshContextsDisplayed();
    }
}
