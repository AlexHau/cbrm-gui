package dke.cbrm.business;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import dke.cbrm.cli.CBRInterface;
import dke.cbrm.persistence.model.Context;
import dke.cbrm.persistence.model.ContextModel;
import dke.cbrm.persistence.model.DetParamValue;
import dke.cbrm.persistence.model.ModificationApproval;
import dke.cbrm.persistence.model.ModificationOperation;
import dke.cbrm.persistence.model.ModificationOperationType;
import dke.cbrm.persistence.model.Parameter;
import dke.cbrm.persistence.model.ParentChildRelation;
import dke.cbrm.persistence.model.Rule;
import dke.cbrm.persistence.parser.FloraFileParser;
import dke.cbrm.persistence.repositories.ContextModelRepository;
import dke.cbrm.persistence.repositories.ContextRepository;
import dke.cbrm.persistence.repositories.DetParamValueRepository;
import dke.cbrm.persistence.repositories.ModificationApprovalRepository;
import dke.cbrm.persistence.repositories.ModificationOperationRepository;
import dke.cbrm.persistence.repositories.ParameterRepository;
import dke.cbrm.persistence.repositories.RuleRepository;
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

    private final CBRInterface cbrInterface;

    private FloraFileParser floraFileParser;

    public void processNewContextModel(ContextModel ctxModel)
	    throws IOException {
	floraFileParser = new FloraFileParser(ctxModel);
	Set<Context> newContexts = floraFileParser.readFileContentsFromFolder();
	for (Context ctx : newContexts) {
	    addOrUpdateContext(ctx);
	}

	buildContextHierarchy(ctxModel);
	buildParameterHierarchy(ctxModel);

	relateDetParamValuesWithParameters(
		getAllParametersOfContextModel(ctxModel.getName()));
    }

    public Iterator<Context> getAllContextsAvailable() {
	return ctxRepository.findAll().iterator();
    }

    public Iterator<Parameter> getAllParametersAvailable() {
	return parameterRepository.findAll().iterator();
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
		.getChildrenOfParent((parent).getContextId()).iterator();

	while (parentChildIter.hasNext()) {
	    resChildren.add((Context) parentChildIter.next()[0]);
	}

	parent.setChildren(resChildren);
	return parent;
    }

    public Parameter getChildren(Parameter parent) {
	Set<Parameter> resChildren = new HashSet<Parameter>();

	Iterator<Object[]> parentChildIter = parameterRepository
		.getChildrenOfParent(parent.getParameterId()).iterator();

	while (parentChildIter.hasNext()) {
	    resChildren.add((Parameter) parentChildIter.next()[0]);
	}

	parent.setChildren(resChildren);
	return parent;
    }

    public void addOrUpdateContext(Context ctx) {
	ctxRepository.save(ctx);
    }

    public void addOrUpdateParameter(Parameter param) {
	parameterRepository.save(param);
    }

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
	    String contextModel) {
	return Optional.ofNullable(
		ctxRepository.getContextByName(contextName, contextModel));
    }

    public Optional<Parameter> findParameter(String parameterValue) {
	return Optional.ofNullable(
		parameterRepository.getParameterByName(parameterValue));
    }

    public Iterator<Parameter> getAllParametersOfContextModel(
	    String contextModel) {
	Set<Parameter> resultSet = new HashSet<Parameter>();
	Iterator<Object[]> iter = parameterRepository
		.getAllParametersOfContextModel(contextModel).iterator();
	while (iter.hasNext()) {
	    resultSet.add((Parameter) iter.next()[0]);
	}
	return resultSet.iterator();
    }

    /**
     * Relates existing {@link Parameter}-Objects with
     * {@link DetParamValue}-Objects
     * 
     * @throws IOException
     */
    public void relateDetParamValuesWithParameters(Iterator<Parameter> iter)
	    throws IOException {

	Set<DetParamValue> detParamValuesSet = null;
	List<String> detParamsFound = null;

	// Iterator<Parameter> iter =
	// cbrmService.getAllParametersOfContextModel(ctxModel.getName());
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
    private void buildContextHierarchy(ContextModel contextModel)
	    throws IOException {

	cbrInterface.setContextModel(contextModel);
	cbrInterface.loadFile(contextModel);

	Iterator<String[]> iter = cbrInterface.getCtxHierarchy().iterator();

	while (iter.hasNext()) {

	    String[] strArray = iter.next();
	    String subContextName = strArray[0];
	    String superContextName = strArray[1];
	    Context parentContext = null;
	    Context subContext = null;
	    Context origParentContext = null;

	    Optional<Context> pc =
		    findContext(superContextName, contextModel.getName());
	    if (pc.isPresent()) {
		parentContext = pc.get();
	    } else {
		parentContext = new Context();
		parentContext.setValue(superContextName);
	    }

	    Optional<Context> sc =
		    findContext(subContextName, contextModel.getName());
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
	    cbrInterface.setContextModel(ctxModel);
	    cbrInterface.loadFile(ctxModel);

	    List<String> rootParameters = cbrInterface.getParameters();

	    for (String rootParameterValue : rootParameters) {
		List<String[]> valueHierarchy = cbrInterface
			.getParameterValuesHiearchy(rootParameterValue);

		LocalDateTime now = LocalDateTime.now();
		Parameter rootParam = new Parameter();
		rootParam.setValue(rootParameterValue);
		rootParam.setCreatedAt(now);
		rootParam.setModifiedAt(now);
		rootParam.setBelongsToContextModel(ctxModel);

		Parameter rootChildParam = null;

		for (String[] valueRelation : valueHierarchy) {

		    Parameter parentParam = null;
		    Optional<Parameter> parentParamOpt =
			    findParameter(valueRelation[0]);

		    if (parentParamOpt.isPresent()) {
			parentParam = parentParamOpt.get();
		    } else {
			parentParam = new Parameter();
			parentParam.setValue(valueRelation[0]);
			now = LocalDateTime.now();
			parentParam.setCreatedAt(now);
			parentParam.setModifiedAt(now);
			parentParam.setBelongsToContextModel(ctxModel);
		    }

		    if (parentParam.getParent() == null) {
			rootChildParam = parentParam;
		    }

		    Parameter origParentParam = null;
		    Parameter subParam = new Parameter();
		    Optional<Parameter> subParamOpt =
			    findParameter(valueRelation[1]);
		    if (subParamOpt.isPresent()) {
			subParam = subParamOpt.get();
			origParentParam = subParam.getParent();
		    } else {
			subParam = new Parameter();
			subParam.setValue(valueRelation[1]);
			now = LocalDateTime.now();
			subParam.setCreatedAt(now);
			subParam.setModifiedAt(now);
			subParam.setParent(parentParam);
			subParam.setBelongsToContextModel(ctxModel);
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

    private Set<ModificationApproval> createApprovalsForContextAndModificationOperation(
	    ModificationOperation modOp, Set<Context> ctxChildren,
	    LocalDateTime now) {
	Set<ModificationApproval> resList = new HashSet<ModificationApproval>();
	for (Context context : ctxChildren) {
	    ModificationApproval modOpApp = new ModificationApproval();
	    modOpApp.setCreatedAt(now);
	    modOpApp.setModifiedAt(now);

	    modOpApp.setApprovedContext(context);
	    modOpApp.setModificationOperationApproved(modOp);
	    resList.add(modOpApp);
	}
	return resList;
    }

    public ModificationOperation createModificationOperation(String value,
	    String string, ModificationOperationType addRule,
	    Context contextSelected) {
	LocalDateTime now = LocalDateTime.now();
	ModificationOperation modOp = new ModificationOperation();
	modOp.setContentBefore("");
	modOp.setContentAfter(value);
	modOp.setCreatedAt(now);
	modOp.setModifiedAt(now);
	modOp.setModificationOperationType(ModificationOperationType.ADD_RULE);
	modOp.getApprovals()
		.addAll(createApprovalsForContextAndModificationOperation(modOp,
			contextSelected.getChildren(), now));
	addOrUpdateModificationOperation(modOp);

	return modOp;
    }

}
