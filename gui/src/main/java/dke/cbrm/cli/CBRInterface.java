package dke.cbrm.cli;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import dke.cbrm.persistence.model.ContextModel;
import lombok.Setter;

/**
 * Specific wrapper for CBR
 * 
 * @author fburgstaller
 *
 */
@Service
@Scope("prototype")
public class CBRInterface extends Flora2CLI {

    @Setter
    private ContextModel contextModel;

    /**
     * Initializes a Flora 2 shell with the CBR Model and the business
     * cases, the contextClass specifies the name of the context class
     * used (AIMCtx)
     * 
     */
    public CBRInterface(@Value("${cbrm.flora.run.path}") String floraRunPath
    // ,@Value("${cbrm.context.model.aim.path}") String contextModelAim,
    // @Value("${cbrm.business.context.model.path}") String
    // businessContext,
    // @Value("${cbrm.context.class}") String contextModel
    // ,@Value("${cbrm.business.case.class}") String
    // contextModel.getBusinessCaseClass()
    ) throws IOException {

	super(floraRunPath);

	// this.contextModel.getBusinessCaseClass() =
	// contextModel.getBusinessCaseClass();
	// this.contextModel = contextModel;

	/**
	 * if (!loadFile(businessContext, BC_MODULE)) throw new
	 * IOException("Loading module failed");
	 **/
    }

    public void loadFile(ContextModel contextModel) throws IOException {
	if (!loadFile(contextModel.getContextModelFilePath(),
		contextModel.getContextModelModuleName()))
	    throw new IOException("Loading module failed");
	if (StringUtils.isNotEmpty(contextModel.getBusinessContextFilePath())
		&& StringUtils.isNotEmpty(
			contextModel.getBusinessContextModuleName())) {
	    loadFile(contextModel.getBusinessContextFilePath(),
		    contextModel.getBusinessContextModuleName());
	    // throw new IOException("Loading module failed");
	}
    }

    // -----------------------------------------------------------------------------
    // ------------------ Query model information
    // -----------------------------------------------------------------------------
    /**
     * Retrieve the contexts in the model
     * 
     * @return a list of contexts
     * @throws IOException
     */
    public List<String> getCtxs() throws IOException {
	String cmd = String.format("?ctx:%s@%s.", contextModel.getName(),
		contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return parseSingleVar(ret);
    }

    /**
     * Retrieves all super-sub context tuples of the model
     * 
     * @return List of context-pairs [subCtx,superCtx]
     * @throws IOException
     */
    public List<String[]> getCtxHierarchy() throws IOException {
	String cmd = String.format(
		"?subCtx:%s[specialises->?superCtx]@%s, \\+ (?superCtx =?subCtx).",
		contextModel.getName(),
		contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return parseMultipleVars(ret, 2);
    }

    /**
     * Get the file location of the rule file belonging to context ctx
     * 
     * @param ctx
     * @return file path of rule file
     * @throws IOException
     */
    public String getCtxFile(String ctx) throws IOException {
	String cmd = String.format("%s:%s[file->?ctxf]@%s.", ctx,
		contextModel.getName(),
		contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return parseSingleVar(ret).get(0);
    }

    /**
     * @param ctx
     * @return
     * @throws IOException
     */
    public List<String[]> getCtx(String ctx) throws IOException {
	String cmd = String.format("(%s:%s[?val:Parameter->?ctx])@%s.", ctx,
		contextModel.getName(),
		contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return parseMultipleVars(ret, 2);
    }

    /**
     * Get the parameters of the CBR model
     * 
     * @return list of parameters
     * @throws IOException
     */
    public List<String> getParameters() throws IOException {
	String cmd = String.format("?param:Parameter@%s.",
		contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return parseSingleVar(ret);
    }

    /**
     * Get all parameter values of the CBR model
     * 
     * @return
     * @throws IOException
     */
    public List<String> getParameterValues() throws IOException {
	String cmd = String.format("(?val:?_param,?_param:Parameter)@%s.",
		contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return parseSingleVar(ret);
    }
    
    /**
     * Find context constituting parameter-values for given context
     * 
     * @param ctx, the given context-name for identifying parameter-values
     * @return parameter-values constituting the given context
     * @throws IOException
     */
    public List<String[]> getCtxInfo(String ctx) throws IOException {
	String cmd = String.format("(%s:%s[?val:Parameter->?ctx])@%s.", ctx,
		contextModel.getName(),
		contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return parseMultipleVars(ret, 2);
    }

    /**
     * Retrieves all super-sub parameter value tuples of the model
     * 
     * @parameter parameter name
     * @return List of parameter-value-pairs [superValue,subValue]
     * @throws IOException
     */
    public List<String[]> getParameterValuesHiearchy(String parameter)
	    throws IOException {
	String cmd = String.format(
		"?superVal:%s[covers->?subVal:%s]@%s, \\+ (?superVal = ?subVal).",
		parameter, parameter, contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return parseMultipleVars(ret, 2);
    }

    // -----------------------------------------------------------------------------
    // ------------------ Evaluate business cases
    // -----------------------------------------------------------------------------
    /**
     * Retrieve relevant contexts for given business case.
     * 
     * @param bc
     * @return
     * @throws IOException
     */
    public List<String> detRelevantCtxs(String bc) throws IOException {
	String cmd = String.format("%s[detRelevantCtxs(%s)->?ctx]@%s.",
		contextModel, bc, contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return parseSingleVar(ret);
    }

    /**
     * Merges rules/terms of all contexts relevant to bc into the
     * specified targetModule.
     * 
     * @param bc
     * @param targetModule
     * @return
     * @throws IOException
     */
    public List<String> detCaseSpecificCtx(String bc, String targetModule)
	    throws IOException {
	checkTargetModule(targetModule);
	String cmd = String.format(
		"%s[%%detCleanCaseSpecificCtx(%s,%s,?ctxf)]@%s.", contextModel,
		bc, targetModule, contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return parseSingleVar(ret);

    }

    /**
     * creates a new business case
     * 
     * @param bcDef
     * @return
     * @throws IOException
     */
    public boolean newBusinessCase(String bcDef) throws IOException {
	String cmd = String.format("%s[%%newBC(%s)]@%s.",
		contextModel.getBusinessCaseClass(), bcDef,
		contextModel.getBusinessContextModuleName());
	String ret = issueCommand(cmd);
	return ret.endsWith("Yes\n");

    }

    /**
     * creates/empties the target module
     * 
     * @param targetModule
     * @return
     * @throws IOException
     */
    private boolean checkTargetModule(String targetModule) throws IOException {
	String cmd = String.format(
		"\\if isloaded{%s} \\then erasemodule{%s} \\else newmodule{%s}.",
		targetModule, targetModule, targetModule);
	String ret = issueCommand(cmd);
	return ret.equals("Yes\n");
    }

    // -----------------------------------------------------------------------------
    // -------------------- Modification operations
    // -----------------------------------------------------------------------------

    // ---------------context
    /**
     * Adds the flora2 context spec to the model
     * 
     * @param ctxDef
     * @return whether it works or not
     * @throws IOException
     */
    public boolean addCtx(String ctxDef) throws IOException {
	String cmd =
		String.format("%s[%%addCtx(%s)]@%s.", contextModel.getName(),
			ctxDef, contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return ret.equals("Yes\n");
    }

    /**
     * Deletes the context with the name ctx from the model
     * 
     * @param ctx
     * @return
     * @throws IOException
     */
    public boolean delCtx(String ctx) throws IOException {
	String cmd =
		String.format("%s[%%delCtx(%s)]@%s.", contextModel.getName(),
			ctx, contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return ret.equals("Yes\n");
    }

    /**
     * Deletes all contexts refering to a non-existing parameter value
     * 
     * @return
     * @throws IOException
     */
    public boolean delCtxByParameterValue() throws IOException {
	String cmd =
		String.format("%s[%%delCtxByValue]@%s.", contextModel.getName(),
			contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return ret.endsWith("Yes\n");
    }

    /**
     * Changes all contexts refering to a non-existing parameter value
     * (oldParamValue) to the new parameter value
     * 
     * @param oldParamValue
     * @param newParamValue
     * @return
     * @throws IOException
     */
    public boolean modifyCtxByParameterValue(String oldParamValue,
	    String newParamValue) throws IOException {
	String cmd = String.format("%s[%%modCtxByValue(%s,%s,?ctx)]@%s.",
		contextModel, oldParamValue, newParamValue,
		contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return ret.endsWith("Yes\n");
    }

    // ---------------parameter
    /**
     * add a parameter with name and parameter root value rootValue and
     * the detParam method specified in Flora2 in parameter detParamDef
     * 
     * @param name
     * @param rootValue
     * @param detParamDef
     * @return
     * @throws IOException
     */
    public boolean addParameter(String name, String rootValue,
	    String detParamDef) throws IOException {
	String cmd = String.format("%s[%%addParam(%s,%s,?ctx,%s)]@%s.",
		contextModel.getName(), name, rootValue, detParamDef,
		contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return ret.endsWith("Yes\n");
    }

    /**
     * Deletes the parameter name
     * 
     * @param name
     * @return
     * @throws IOException
     */
    public boolean delParameter(String name) throws IOException {
	String cmd = String.format("%s[%%delParam(%s,?ctx)]@%s.",
		contextModel.getName(), name,
		contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return ret.endsWith("Yes\n");
    }

    // ---------------parameter value
    /**
     * Adds a parameter leaf value under the value parent
     * 
     * @param name
     * @param parent
     * @return
     * @throws IOException
     */
    public boolean addParameterValueLeaf(String name, String parent)
	    throws IOException {
	String cmd =
		String.format("%s[%%addValueLeaf(%s,%s)]@%s.", contextModel,
			name, parent, contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return ret.endsWith("Yes\n");
    }

    /**
     * Add a parameter value in the parameter value tree between parent
     * and children
     * 
     * @param name
     * @param parent
     * @param children
     * @return
     * @throws IOException
     */
    public boolean addParameterValueNode(String name, String parent,
	    String[] children) throws IOException {
	StringBuilder childs = new StringBuilder(80);
	for (String child : children) {
	    childs.append(child + ",");
	}
	childs.delete(childs.length() - 1, childs.length());
	String cmd = String.format("%s[%%addValueNode(%s,%s,{%s})]@%s.",
		contextModel, name, parent, childs,
		contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return ret.endsWith("Yes\n");
    }

    /**
     * Add a new rootValue above child
     * 
     * @param name
     * @param child
     * @return
     * @throws IOException
     */
    public boolean addParameterValueRoot(String name, String child)
	    throws IOException {
	String cmd =
		String.format("%s[%%addValueRoot(%s,%s)]@%s.", contextModel,
			name, child, contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return ret.endsWith("Yes\n");
    }

    /**
     * Delete parameter value name and all its subvalues
     * 
     * @param name
     * @return
     * @throws IOException
     */
    public boolean delParameterValueSubgraph(String name) throws IOException {
	String cmd = String.format("%s[%%delValueSubGraph(%s)]@%s.",
		contextModel, name, contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return ret.endsWith("Yes\n");
    }

    /**
     * Delete a single parameter value
     * 
     * @param name
     * @return
     * @throws IOException
     */
    public boolean delParameterValue(String name) throws IOException {
	String cmd = String.format("%s[%%delValue(%s,?parent)]@%s.",
		contextModel, name, contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return ret.endsWith("Yes\n");
    }

    /**
     * Delete a single parameter value
     * 
     * @param name
     * @return
     * @throws IOException
     */
    public List<String> detUnusedParameterValues() throws IOException {
	String cmd = String.format("%s[detUnusedValues->?v]@%s.", contextModel,
		contextModel.getContextModelModuleName());
	String ret = issueCommand(cmd);
	return parseSingleVar(ret);

    }
}
