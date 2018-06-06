package dke.cbrm.business;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.NonUniqueResultException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

import dke.cbrm.cli.CBRInterface;
import dke.cbrm.persistence.model.Context;
import dke.cbrm.persistence.model.ContextModel;
import dke.cbrm.persistence.model.DetParamValue;
import dke.cbrm.persistence.model.ModificationApproval;
import dke.cbrm.persistence.model.ModificationOperation;
import dke.cbrm.persistence.model.ModificationOperationType;
import dke.cbrm.persistence.model.Modifieable;
import dke.cbrm.persistence.model.Parameter;
import dke.cbrm.persistence.model.ParentChildRelation;
import dke.cbrm.persistence.model.Rule;
import dke.cbrm.persistence.model.User;
import dke.cbrm.persistence.parser.FloraFileParser;
import dke.cbrm.persistence.repositories.ContextModelRepository;
import dke.cbrm.persistence.repositories.ContextRepository;
import dke.cbrm.persistence.repositories.DetParamValueRepository;
import dke.cbrm.persistence.repositories.ModificationApprovalRepository;
import dke.cbrm.persistence.repositories.ModificationOperationRepository;
import dke.cbrm.persistence.repositories.ParameterRepository;
import dke.cbrm.persistence.repositories.RuleRepository;
import dke.cbrm.persistence.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CbrmService {

    private final ContextModelRepository ctxModelRepository;

    private final ContextRepository ctxRepository;

    private final RuleRepository ruleRepository;

    private final ParameterRepository parameterRepository;

    private final DetParamValueRepository detParamValueRepository;

    private final ModificationOperationRepository modificationOperationRepository;

    private final ModificationApprovalRepository modificationApprovalRepository;

    private final UserRepository userRepository;

    private final CBRInterface cbrInterface;

    private FloraFileParser floraFileParser;

    public void processNewContextModel(ContextModel ctxModel)
	    throws IOException {
	floraFileParser = new FloraFileParser(ctxModel);

	cbrInterface.setContextModel(ctxModel);
	cbrInterface.loadFile(ctxModel);

	ctxModel.setContexts(floraFileParser.readFileContentsFromFolder());
	addOrUpdateContextModel(ctxModel);

	buildContextHierarchy(ctxModel);

	buildParameterHierarchy(ctxModel);

	buildContextParamaterRelations(ctxModel);

	relateDetParamValuesWithParameters(ctxModel);
    }

    public Iterator<Context> getAllContextsOfContextModel(
	    ContextModel ctxModel) {
	return ctxRepository.getContextsForContextModel(ctxModel.getName())
		.iterator();
    }

    public Iterator<Parameter> getAllParametersAvailable() {
	return parameterRepository.findAll().iterator();
    }

    public Iterator<Parameter> getParametersOfContextModel(
	    ContextModel ctxModel) {
	return parameterRepository
		.getAllParametersOfContextModel(ctxModel.getName()).iterator();
    }

    public Iterator<ContextModel> getAllContextModelsAvailable() {
	return ctxModelRepository.findAll().iterator();
    }

    public Iterator<DetParamValue> getDetParamValuesByParameterId(
	    Long parameterId) {
	List<DetParamValue> resList = new ArrayList<DetParamValue>();
	Iterator<Object[]> resObjectsIter = detParamValueRepository
		.findByParameterId(parameterId).iterator();
	while (resObjectsIter.hasNext()) {
	    resList.add((DetParamValue) resObjectsIter.next()[0]);
	}
	return resList.iterator();
    }

    public Iterable<Rule> getRulesByContextName(String contextName,
	    String currentContextModel) {
	List<Rule> resList = new ArrayList<Rule>();

	Iterator<Object[]> resIter = ruleRepository
		.findByContextName(contextName, currentContextModel).iterator();
	while (resIter.hasNext()) {
	    Rule rule = (Rule) resIter.next()[0];
	    resList.add(rule);
	}
	return resList;
    }

    public Context getChildren(Context parent) {
	Set<Context> resChildren = new HashSet<Context>();

	Iterator<Object[]> parentChildIter = ctxRepository
		.getChildrenOfParent(parent.getContextId()).iterator();

	while (parentChildIter.hasNext()) {
	    resChildren.add((Context) parentChildIter.next()[0]);
	}

	parent.setChildren(resChildren);
	return parent;
    }

    public ContextModel getChildren(ContextModel ctxModel) {
	Iterator<Context> iter = ctxModelRepository
		.getAllContextsOfContextModel(ctxModel.getId()).iterator();
	ctxModel.setContexts(Sets.newHashSet(iter));
	return ctxModel;
    }

    public Context getParameters(Context ctx) {
	Set<Parameter> resSet = Sets.newHashSet(parameterRepository
		.getChildrenOfParent(ctx.getContextId()).iterator());
	// while (iter.hasNext()) {
	// resSet.add((Parameter) iter.next()[0]);
	// }
	ctx.setConstitutingParameterValues(resSet);
	return ctx;
    }

    public Parameter getChildren(Parameter parent) {
	Set<Parameter> resChildren = Sets.newHashSet(parameterRepository
		.getChildrenOfParent(parent.getParameterId()).iterator());

	// while (parentChildIter.hasNext()) {
	// resChildren.add((Parameter) parentChildIter.next()[0]);
	// }

	parent.setChildren(resChildren);
	return parent;
    }

    public Context loadParametersOfContext(Context ctx) {
	Iterable<Parameter> paramsIter =
		parameterRepository.getParametersOfContext(ctx.getContextId());
	ctx.setConstitutingParameterValues(
		Sets.newHashSet(paramsIter.iterator()));
	return ctx;
    }

    public Parameter getParamaterFromParameterValue(Parameter param) {
	return parameterRepository.getParentParameter(param.getParameterId());
    }

    public void addOrUpdateContext(Context ctx) {
	ctxRepository.save(ctx);
    }

    public void addOrUpdateParameter(Parameter param) {
	parameterRepository.save(param);
    }

    @Transactional
    public void addOrUpdateContextModel(ContextModel ctxModel) {
	ctxModelRepository.save(ctxModel);
    }

    public void addOrUpdateDetParamValue(DetParamValue detParamValue) {
	detParamValueRepository.save(detParamValue);
    }

    public void addOrUpdateRule(Rule rule) {
	ruleRepository.save(rule);
    }

    public void addOrUpdateModificationApproval(
	    ModificationApproval modificationApproval) {
	modificationApprovalRepository.save(modificationApproval);
    }

    private void addOrUpdateModificationOperation(ModificationOperation modOp) {
	modificationOperationRepository.save(modOp);
    }

    public Optional<Context> findContext(String contextName,
	    Long contextModelId) throws NonUniqueResultException {
	return Optional.ofNullable(ctxRepository
		.getOnlyValidContextByName(contextName, contextModelId));
    }

    public Optional<Parameter> findParameter(String parameterValue,
	    String contextModelName) {
	return Optional.ofNullable(parameterRepository
		.getParameterByName(parameterValue, contextModelName));
    }

    public Iterator<Parameter> getAllParametersOfContextModel(
	    ContextModel contextModel) {
	return parameterRepository
		.getAllParametersOfContextModel(contextModel.getName())
		.iterator();
    }

    /**
     * Relates existing {@link Parameter}-Objects with
     * {@link DetParamValue}-Objects
     * 
     * @throws IOException
     */
    public void relateDetParamValuesWithParameters(ContextModel ctxModel)
	    throws IOException {

	Iterator<Parameter> iter = getAllParametersOfContextModel(ctxModel);
	Set<DetParamValue> detParamValuesSet = null;
	List<String> detParamsFound = null;

	while (iter.hasNext()) {
	    Parameter parameter = iter.next();
	    if (parameter.getParent() == null) {
		detParamValuesSet = new HashSet<DetParamValue>();
		detParamsFound = floraFileParser
			.getDetParamValueForParameter(parameter.getValue());
		LocalDateTime now = LocalDateTime.now();

		for (String value : detParamsFound) {
		    DetParamValue detParamValue = new DetParamValue();

		    detParamValue.setCreatedAt(now);
		    detParamValue.setModifiedAt(now);
		    detParamValue.setValidFrom(now);
		    detParamValue.setContent(value);
		    detParamValue.setParameter(parameter);

		    detParamValuesSet.add(detParamValue);
		}
		parameter.setDetParamValues(detParamValuesSet);
		this.addOrUpdateParameter(parameter);
	    }
	}
    }

    /**
     * Creates or updates a given Parent-Context given the
     * 'superContextName' and its Child- / Sub-Context and persists into
     * repository
     * 
     * @param superContextName
     * @param subContextName
     * @throws IOException
     */
    @Transactional
    private void buildContextHierarchy(ContextModel contextModel)
	    throws IOException {
	Iterator<String[]> iter = cbrInterface.getCtxHierarchy().iterator();

	while (iter.hasNext()) {

	    String[] strArray = iter.next();
	    String subContextName = strArray[0];
	    String superContextName = strArray[1];
	    Context parentContext = null;
	    Context subContext = null;
	    Context origParentContext = null;

	    try {
		Optional<Context> pc =
			findContext(superContextName, contextModel.getId());

		if (pc.isPresent()) {
		    parentContext = pc.get();
		} else {
		    parentContext = new Context();
		    parentContext.setValue(superContextName);
		}

		Optional<Context> sc =
			findContext(subContextName, contextModel.getId());
		if (sc.isPresent()) {
		    subContext = sc.get();
		    origParentContext = subContext.getParent();
		} else {
		    subContext = new Context();
		    subContext.setCreatedAt(LocalDateTime.now());
		    subContext.setValue(subContextName);
		}

		/** Put Context into Parent and child relation **/
		if (origParentContext == null || (!origParentContext.getParent()
			.getValue().equals(superContextName)
			&& newParentLowerInHierarchy(origParentContext,
				parentContext))) {

		    subContext.setParent(parentContext);
		    subContext.setModifiedAt(LocalDateTime.now());

		    parentContext = getChildren(parentContext);
		    parentContext.getChildren().add(subContext);
		    if (origParentContext != null) {
			origParentContext.getChildren().remove(subContext);
			origParentContext.setModifiedAt(LocalDateTime.now());

			addOrUpdateContext(origParentContext);
		    }
		}

		/** save / update the creations / updates in repository **/
		addOrUpdateContext(parentContext);
		addOrUpdateContext(subContext);

	    } catch (NonUniqueResultException e) {
		e.printStackTrace();
	    }

	}
    }

    /**
     * Determines if a given {@paramRef parentParam} is lower in hierarchy
     * {@paramRef origParam}: i.e.: if there is already a parent from
     * {@paramRef parentParam} who´s name is equal to
     * {@paramRef origParam}´s name, this method returns true
     * 
     * @param origParam
     * @param parentParameter
     * @return true, if new {@paramRef parentParameter} is lower in
     *         hierarchy than old {@paramRef origParam}
     */
    @SuppressWarnings("rawtypes")
    private boolean newParentLowerInHierarchy(ParentChildRelation origParam,
	    ParentChildRelation parentParameter) {
	while ((parentParameter =
		(ParentChildRelation) parentParameter.getParent()) != null) {
	    if (parentParameter.getValue().equals(origParam.getValue()))
		return true;
	}
	return false;
    }

    public void buildParameterHierarchy(ContextModel ctxModel) {
	try {
	    ctxModel.setParameters(
		    Sets.newHashSet(getAllParametersOfContextModel(ctxModel)));
	    List<String> rootParameters = cbrInterface.getParameters();

	    for (String rootParameterValue : rootParameters) {
		List<String[]> valueHierarchy = cbrInterface
			.getParameterValuesHiearchy(rootParameterValue);

		LocalDateTime now = LocalDateTime.now();
		Parameter rootParam = new Parameter();
		rootParam.setValue(rootParameterValue);
		rootParam.setCreatedAt(now);
		rootParam.setModifiedAt(now);
		rootParam.setValidFrom(now);
		rootParam.setBelongsToContextModel(ctxModel);
		ctxModel.getParameters().add(rootParam);

		Parameter rootChildParam = null;

		for (String[] valueRelation : valueHierarchy) {

		    Parameter parentParam = null;
		    Optional<Parameter> parentParamOpt =
			    findParameter(valueRelation[0], ctxModel.getName());

		    if (parentParamOpt.isPresent()) {
			parentParam = parentParamOpt.get();
		    } else {
			parentParam = new Parameter();
			parentParam.setValue(valueRelation[0]);
			now = LocalDateTime.now();
			parentParam.setCreatedAt(now);
			parentParam.setModifiedAt(now);
			parentParam.setValidFrom(now);
			parentParam.setBelongsToContextModel(ctxModel);
			ctxModel.getParameters().add(parentParam);
		    }

		    if (parentParam.getParent() == null) {
			rootChildParam = parentParam;
		    }

		    Parameter origParentParam = null;
		    Parameter subParam = new Parameter();
		    Optional<Parameter> subParamOpt =
			    findParameter(valueRelation[1], ctxModel.getName());
		    if (subParamOpt.isPresent()) {
			subParam = subParamOpt.get();
			origParentParam = subParam.getParent();
		    } else {
			subParam = new Parameter();
			subParam.setValue(valueRelation[1]);
			now = LocalDateTime.now();
			subParam.setCreatedAt(now);
			subParam.setModifiedAt(now);
			subParam.setValidFrom(now);
			subParam.setParent(parentParam);
			subParam.setBelongsToContextModel(ctxModel);
			ctxModel.getParameters().add(subParam);
		    }

		    /**
		     * Decide, which Parent is the closest Node to subParam if
		     * subParam already existed but should have a different
		     * parent according to flora-shell output
		     **/
		    if (origParentParam == null || (!origParentParam.getValue()
			    .equals(valueRelation[0])
			    && newParentLowerInHierarchy(origParentParam,
				    parentParam))) {
			subParam.setParent(parentParam);
			if (origParentParam != null) {
			    origParentParam = getChildren(origParentParam);
			    origParentParam.getChildren().remove(subParam);
			}
		    }

		    addOrUpdateParameter(parentParam);
		    addOrUpdateParameter(subParam);
		    if (origParentParam != null) {
			addOrUpdateParameter(origParentParam);
		    }
		}
		addOrUpdateParameter(rootParam);

		rootParam.getChildren().add(rootChildParam);
		rootChildParam.setParent(rootParam);
		addOrUpdateParameter(rootChildParam);

	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void deleteRule(Context ctx, Rule rule) {
	ctx.setRules(Sets.newHashSet(this.getRulesByContextName(ctx.getValue(),
		ctx.getInstantiatesContextModel().getName())));
	ctx.getRules().remove(rule);
	ctxRepository.save(ctx);
	ruleRepository.delete(rule);
    }

    private void buildContextParamaterRelations(ContextModel ctxModel) {
	Iterator<Context> iter = ctxRepository
		.getContextsForContextModel(ctxModel.getName()).iterator();

	while (iter.hasNext()) {
	    Context ctx = iter.next();
	    ctx = getParameters(ctx);
	    System.out.println(
		    ctx.getValue() + " has following Parameter Values: ");

	    List<String[]> parametersOfContext;
	    try {
		parametersOfContext = cbrInterface.getCtxInfo(ctx.getValue());
		for (int i = 0; i < parametersOfContext.size(); i++) {
		    String[] array = parametersOfContext.get(i);
		    String parameterSearched = array[1];
		    System.out.println(parameterSearched);

		    Parameter param = parameterRepository.getParameterByName(
			    parameterSearched, ctxModel.getName());
		    ctx.getConstitutingParameterValues().add(param);
		}
		addOrUpdateContext(ctx);
	    } catch (IOException e) {

	    }
	}
    }

    private Set<ModificationApproval> createApprovalsForContextAndModificationOperation(
	    ModificationOperation modOp, Modifieable modifiedObject,
	    Context contextAffected, LocalDateTime now) {
	Set<ModificationApproval> resList = new HashSet<ModificationApproval>();

	if (contextAffected != null) {
	    for (Context context : contextAffected.getChildren()) {
		ModificationApproval modOpApp = new ModificationApproval();
		modOpApp.setCreatedAt(now);
		modOpApp.setModifiedAt(now);

		modOpApp.setApprovedContext(context);
		modOpApp.setModificationOperationApproved(modOp);
		resList.add(modOpApp);
	    }
	}

	return resList;
    }

    public ModificationOperation createModificationOperation(
	    ModificationOperationType modOpType, Modifieable modifiedObject,
	    Context contextAffected, Rule ruleAffected,
	    Parameter parameterAffected) {
	LocalDateTime now = LocalDateTime.now();
	ModificationOperation modOp = new ModificationOperation();
	modOp.setCreatedAt(now);
	modOp.setModifiedAt(now);
	modOp.setCreatedBy(userRepository.findByUserName(SecurityContextHolder
		.getContext().getAuthentication().getName()));
	modOp.setModificationOperationType(modOpType);

	// modOp.getApprovals()
	// .addAll(createApprovalsForContextAndModificationOperation(modOp,
	// modifiedObject, contextAffected, now));

	switch (modOpType) {
	case ADD_CONTEXT:
	    modOp.setContextAffected(contextAffected);
	    break;
	case ADD_CONTEXT_CLASS:
	    break;
	case ADD_PARAMETER:
	    modOp.setParameterAffected((Parameter) modifiedObject);
	    break;
	case ADD_RULE:
	    modOp.setRuleAffected((Rule) modifiedObject);
	    break;
	case DELETE_CONTEXT:
	    modOp.setContextAffected(contextAffected);
	    break;
	case DELETE_CONTEXT_CLASS:
	    break;
	case DELETE_PARAMETER:
	    modOp.setParameterAffected((Parameter) modifiedObject);
	    break;
	case DELETE_RULE:
	    modOp.setRuleAffected((Rule) modifiedObject);
	    break;
	case MODIFY_PARAMETER:
	    modOp.setParameterAffected((Parameter) modifiedObject);
	    break;
	default:
	    break;
	}

	modOp.setContextAffected(contextAffected);
	addOrUpdateModificationOperation(modOp);

	return modOp;
    }

    /**
     * Evaluates a given @link{ModificationOperation} regarding
     * allowedUsers of a @link{Context}
     * 
     * @param modOp
     *            the @link{ModificationOperataion} to evaluate
     * @return affected Users
     */
    public Set<User> getAffectedUsersFromModOp(ModificationOperation modOp) {
	Set<User> affectedUsers = new HashSet<User>();
	if (modOp.getContextAffected() != null) {
	    addUsersFromNextContextHierarchy(modOp.getContextAffected(),
		    affectedUsers);
	}
	return affectedUsers;
    }

    /**
     * Recursively adds assigned {@link User}s from all children-contexts
     * beginning with given @{link Context}
     * 
     * @param ctx
     * @param affectedUsers
     */
    private void addUsersFromNextContextHierarchy(Context ctx,
	    Set<User> affectedUsers) {
	affectedUsers.addAll(Sets.newHashSet(
		userRepository.getAllowedUsersByContextId(ctx.getContextId())));
	ctx = getChildren(ctx);
	for (Context childCtx : ctx.getChildren()) {
	    addUsersFromNextContextHierarchy(childCtx, affectedUsers);
	}
    }

    public Set<ModificationOperation> getModificationOperationsForUser(
	    String userName) {
	Set<ModificationOperation> res = new HashSet<ModificationOperation>();
	Set<ModificationOperation> initialModOpsNotApproved =
		Sets.newHashSet(modificationOperationRepository
			.getNotApprovedAndInitialModOps());

	for (ModificationOperation modOp : initialModOpsNotApproved) {
	    if (modOp.getContextAffected() != null) {
		Set<User> affectedUsers = getAffectedUsersFromModOp(modOp);
		if (affectedUsers.stream()
			.filter(x -> x.getUserName().equals(userName)).findAny()
			.isPresent()) {
		    res.add(modOp);
		}
	    }
	}

	return res;
    }

    /**
     * @param initialContext
     * @{link Context} also holding constitutingParameters
     * @return Map with Key for Parameter and Values being further Map
     *         with Key being the Parameter-Value highest in hierarchy
     *         followed by a Set of parameter values on levels below
     */
    public Map<Parameter, Map<Parameter, Set<Parameter>>> getChildrenParametersForContext(
	    Context initialContext) {
	Map<Parameter, Map<Parameter, Set<Parameter>>> resultMap =
		new HashMap<Parameter, Map<Parameter, Set<Parameter>>>();

	initialContext = loadParametersOfContext(initialContext);

	// Set<Parameter> rootParams =
	// Sets.newHashSet(parameterRepository.getRootParentParameters(
	// initialContext.getInstantiatesContextModel().getId()));

	for (Parameter param : initialContext
		.getConstitutingParameterValues()) {
	    Parameter p = getParamaterFromParameterValue(param);

	    if (p != null) {
		Set<Parameter> childrenParameter = Sets
			.newHashSet(parameterRepository.getParameterValuesBelow(
				param.getParameterId()));
		Map<Parameter, Set<Parameter>> paramValuesMap =
			new HashMap<Parameter, Set<Parameter>>();
		paramValuesMap.put(param, childrenParameter);
		resultMap.put(p, paramValuesMap);
	    }
	}

	// for (Parameter param : rootParams) {
	// resultMap.put(param, Sets.newHashSet(parameterRepository
	// .getChildrenOfParent(param.getParameterId())));
	// }
	return resultMap;
    }

    /**
     * Instantiate new Context with given Parameters and parent context
     * 
     * @param values
     *            Parameter values constituting the newly created Context
     * @param initialContext
     *            the parent Context of the new one
     */
    public void createContext(Collection<Parameter> values,
	    Context initialContext) {
	LocalDateTime now = LocalDateTime.now();
	Context ctx = new Context();
	ctx.setCreatedAt(now);
	ctx.setModifiedAt(now);
	ctx.setValidFrom(now);
	ctx.setConstitutingParameterValues(new HashSet<Parameter>(values));
	ctx.setParent(initialContext);
	ctx.setInstantiatesContextModel(
		initialContext.getInstantiatesContextModel());

	addOrUpdateContext(ctx);
    }

    public boolean contextWithParametersExistsYet(ContextModel ctxModel,
	    Collection<Parameter> params) {
	// Set<Long> paramterIds =
	// params.stream().map(Parameter::getParameterId).collect(Collectors.toSet());
	Iterator<Context> ctxIter = parameterRepository
		.findContextWithParameters(ctxModel.getId(), params).iterator();
	while (ctxIter.hasNext()) {
	    System.out.println(ctxIter.next().getValue());
	}
	return ctxIter != null;
    }

}
