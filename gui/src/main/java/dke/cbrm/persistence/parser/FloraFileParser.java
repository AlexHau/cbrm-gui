package dke.cbrm.persistence.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import dke.cbrm.persistence.model.Context;
import dke.cbrm.persistence.model.ContextModel;
import dke.cbrm.persistence.model.Rule;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ahauer
 *
 *         FloraParser´s responsibility is to identify @{link Rule}-
 *         and @{link Context}-Objects in Path / Folder indicated by
 *         {@link #cbrmContextsFolderPath} and to persist them in
 *         repository @{link CbrmRepository}
 */
@Slf4j
public class FloraFileParser {

    public FloraFileParser(ContextModel contextModel) {
	this.ctxModel = contextModel;
    }

    /**
     * Regex-String for searching the entry of parameter-values of
     * parameter within domain context model file. Formatable with first
     * parameter being the parent-parameter and second being the
     * parameter-value searched for
     */
    public static final String PARAMETER_VALUES_OF_PARAMETER =
	    "\\{.*?(,%2$s|,?%2$s).*?\\}:%1$s\\.";

    /**
     * Regex-String for searching the entry of parameter-value-hierarchy
     * definition within domain context model file. Formatable with first
     * parameter being the parent-parameter-value and second being the
     * parameter-value
     */
    public static final String PARAMETER_VALUE_COVERS_RELATION = "%1$s"
	    + "\\[covers\\-\\>\\{.*?" + "(%2$s,|,?%2$s).*?" + "\\}\\]\\.";

    public static final String PARAMETER = "%1$s\\:Parameter\\.";

    /**
     * Regex-Pattern for searching the entry of domain context model
     * parameter defintion within domain context model file. Formatable
     * with first parameter being the parameter
     */
    public static final String CONTEXT_MODEL_PATTERN =
	    ".*?:ContextClass\\[defBy\\-\\>\\{.*?(,%1$s|,?%1$s)\\}\\]\\.";

    /**
     * Regex-String for searching context-id definitions within context
     * file
     */
    private static final Pattern CONTEXT_PATTERN =
	    Pattern.compile("ctx\\[id\\-\\>\\'(.*)\\'\\]\\.");

    /**
     * Regex-String for searching rule definitions within context file
     */
    public static final Pattern RULE_PATTERN =
	    Pattern.compile("@\\!\\{((R|\\_)\\d+)\\}+\\n(.)*?\\.");

    private static final Pattern PARAMETER_HIERARCHY_MARKER_PATTERN =
	    Pattern.compile("//## Parameter-Hierarchy");

    private static final Pattern PARAMETERS_MARKER_PATTERN =
	    Pattern.compile("//## Parameters");

    private static final Pattern PARAMETER_VALUES_MARKER_PATTERN =
	    Pattern.compile("//## Parameter-Values");

    private static final String FLORA_AUX_FILES = ".flora_aux_files";

    private static final String DET_PARAM_VALUE =
	    "%1$s(\\[detParamValue.*?\\])(\\:\\-\\?val\\:%1$s)((,\\(.*?\\)\\@.*?)|(,((get\\w*)\\(.*?\\))))+\\.";

    private static final String CONTEXT_PARAMETER_VALUES = ".*?\\:";

    @Getter
    private String domainContextModelContent, contextContent;

    private ContextModel ctxModel;

    /**
     * Reads all Files under {@link #cbrmContextsFolderPath}, except the
     * non regular Files and the ones in folder ".flora_aux_files" and
     * returns the so built Entity-Objects
     * 
     * @return all built context-Objects from indicated folder
     * 
     * @throws CbrmContextModelNotWritableException
     */
    public Set<Context> readFileContentsFromFolder() {
	Set<Context> resSet = new HashSet<Context>();
	try (Stream<Path> paths = Files.walk(Paths.get(ctxModel.getContextsFolderPath()))) {
	    paths.filter(Files::isRegularFile)
		    .filter(it -> !it.toString().contains(FLORA_AUX_FILES))
		    .forEach(filePath -> {
			log.info("Parsing..." + filePath.toString());
			try {
			    contextContent = readFileContent(filePath);
			} catch (IOException e) {
			    e.printStackTrace();
			}

			resSet.add(extractEntitiesFromContextFileContent(
				filePath.toString()));
		    });
	} catch (IOException e1) {
	    e1.printStackTrace();
	}
	return resSet;
    }

    /**
     * Returns all detParamValue definitions for a given parameter in the
     * domain context model file
     * 
     * @param parameter
     *            parameter the detParam,Value definitions are searched
     *            for
     * @return HashSet holding all detParamValue definitions for given
     *         parameter
     * @throws IOException
     */
    public List<String> getDetParamValueForParameter(String parameter)
	    throws IOException {

	domainContextModelContent =
		readFileContent(Paths.get(ctxModel.getContextModelFilePath()));

	List<String> resultList = new ArrayList<String>();

	Matcher detParamValueMatcher =
		Pattern.compile(String.format(DET_PARAM_VALUE, parameter))
			.matcher(domainContextModelContent);

	while (detParamValueMatcher.find()) {
	    String wholeContent = detParamValueMatcher.group();
	    resultList.add(wholeContent);

	    String extensionNotConsidered = detParamValueMatcher.group(6);
	    String extension = detParamValueMatcher.group(7);

	    if (!StringUtils.isEmpty(extension)) {
		Matcher getMatcher = Pattern
			.compile("(".concat(extension).concat("\\(.*?\\))\\."))
			.matcher(domainContextModelContent);

		while (getMatcher.find()) {
		    String ext = getMatcher.group(0);
		    String compareExt = getMatcher.group(1);
		    if (!compareExt.equals(extensionNotConsidered)) {
			resultList.add(ext);
		    }
		}
	    }
	}

	return resultList;
    }

    /**
     * Adds a new Parameter to the ContextModelClass in domain context
     * model file. It is assumed that a domain context model entry exists
     * in the file
     * 
     * @param newParameter
     *            the new parameter to be added
     * @param rootParameterValue
     * @return success of write operation
     */
    public boolean addNewParameterToContextModel(String newParameter,
	    String rootParameterValue) {

	Matcher contextModelMatcher =
		Pattern.compile(String.format(CONTEXT_MODEL_PATTERN, ""))
			.matcher(domainContextModelContent);
	if (contextModelMatcher.find()) {
	    StringBuffer sb =
		    new StringBuffer(domainContextModelContent.length());
	    String wholeContent = contextModelMatcher.group();
	    wholeContent = wholeContent.replace("}",
		    ",".concat(newParameter).concat("}"));
	    contextModelMatcher.appendReplacement(sb, wholeContent);
	    contextModelMatcher.appendTail(sb);
	    writeChangesToFile(ctxModel.getContextModelFilePath(), sb);
	}

	Matcher parameterMarkerMatcher =
		PARAMETERS_MARKER_PATTERN.matcher(domainContextModelContent);
	if (parameterMarkerMatcher.find()) {
	    StringBuffer sb =
		    new StringBuffer(domainContextModelContent.length());
	    String wholeContent = parameterMarkerMatcher.group();
	    wholeContent = wholeContent.concat(System.lineSeparator()
		    .concat(newParameter).concat(":Parameter."));
	    parameterMarkerMatcher.appendReplacement(sb, wholeContent);
	    parameterMarkerMatcher.appendTail(sb);
	    writeChangesToFile(ctxModel.getContextModelFilePath(), sb);
	}

	Matcher parameterValuesMatcher = PARAMETER_VALUES_MARKER_PATTERN
		.matcher(domainContextModelContent);
	if (parameterValuesMatcher.find()) {
	    StringBuffer sb =
		    new StringBuffer(domainContextModelContent.length());
	    String wholeContent = parameterValuesMatcher.group();
	    wholeContent = wholeContent.concat(System.lineSeparator()
		    .concat("{").concat(rootParameterValue).concat("}:")
		    .concat(newParameter).concat("."));
	    parameterValuesMatcher.appendReplacement(sb, wholeContent);
	    parameterValuesMatcher.appendTail(sb);
	    writeChangesToFile(ctxModel.getContextModelFilePath(), sb);
	}

	return false;
    }

    /**
     * Deletes a given Parameter from domain context model. It´s assumed
     * all dependent children of root parameter value and other parameter
     * values were delete before.
     * 
     * @param parameter
     * @return success of file write operation
     */
    public boolean deleteParameterFromContextModel(String parameter) {
	Matcher parameterMatcher =
		Pattern.compile(String.format(PARAMETER, parameter))
			.matcher(domainContextModelContent);
	if (parameterMatcher.find()) {
	    StringBuffer sb =
		    new StringBuffer(domainContextModelContent.length());
	    String wholeContent = parameterMatcher.group();
	    wholeContent = wholeContent.replaceAll(".*", "");
	    parameterMatcher.appendReplacement(sb, wholeContent);
	    parameterMatcher.appendTail(sb);
	    writeChangesToFile(ctxModel.getContextModelFilePath(), sb);
	}

	Matcher parameterContextModelMatcher = Pattern.compile(
		String.format(FloraFileParser.CONTEXT_MODEL_PATTERN, parameter))
		.matcher(domainContextModelContent);
	if (parameterContextModelMatcher.find()) {
	    StringBuffer sb =
		    new StringBuffer(domainContextModelContent.length());
	    String wholeContent = parameterContextModelMatcher.group();
	    String group = parameterContextModelMatcher.group(1);
	    wholeContent = wholeContent.replace(group, "");
	    parameterContextModelMatcher.appendReplacement(sb, wholeContent);
	    parameterContextModelMatcher.appendTail(sb);
	    writeChangesToFile(ctxModel.getContextModelFilePath(), sb);
	}
	return false;
    }

    /**
     * Writes a new Parameter to context-model file
     * 
     * @param domainContextModelContent
     * @param parameterValue
     * @throws CbrmContextModelNotWritableException
     */
    public void writeNewParameterValueToFile(String parameterValue,
	    String parentParameter, String parentParameterValue) {
	modifyParameterOrValueRelation(parentParameterValue, parameterValue,
		true, true);

	modifyParameterOrValueRelation(parentParameter, parameterValue, true,
		false);
    }

    /**
     * Deletes a parameter-value from the domain context model file
     * 
     * @param parameterToDelete
     *            the parameter to be removed from general
     *            Parameter-definition and covers-relation in .flr-file
     * @param parentParameterValue
     *            indicating the covers-relation of parameter in .flr-file
     */
    public void deleteParameterValue(String parameterToDelete,
	    String parentParameterValue) {
	modifyParameterOrValueRelation(".*", parameterToDelete, false, false);
	modifyParameterOrValueRelation(parentParameterValue, parameterToDelete,
		false, true);
    }

    /**
     * Changes the hierarchy over the covers relation in the flr. context
     * model file
     * 
     * @param movedParameterValue
     * @param newParentParameterValue
     * @param oldParentParameterValue
     * 
     */
    public void changeHierarchyOfParamterValues(String movedParameterValue,
	    String newParentParameterValue, String oldParentParameterValue) {
	modifyParameterOrValueRelation(oldParentParameterValue,
		movedParameterValue, false, true);
	modifyParameterOrValueRelation(newParentParameterValue,
		movedParameterValue, true, true);
    }

    /**
     * Adds or deletes parameter-values to domain context model file
     * 
     * @param parentParameterValue
     *            the parent parameter value to be deleted from or added
     *            to
     * @param parameterValue
     *            the parameter-value to be added or deleted
     * @param add
     *            indicating whether it is a add - or delete modification
     *            regarding the parameterValue
     * @param isCoversRelation
     *            indicating whether it is a modification of a
     *            covers-relation or parameter-values
     * @return true if modification was successfully written to domain
     *         context model file
     */
    private boolean modifyParameterOrValueRelation(String parentParameterValue,
	    String parameterValue, boolean add, boolean isCoversRelation) {

	String wholeContent = null;
	boolean foundMatch = false;

	String regex = String.format(
		isCoversRelation ? PARAMETER_VALUE_COVERS_RELATION
			: PARAMETER_VALUES_OF_PARAMETER,
		parentParameterValue, add ? "" : parameterValue);

	Matcher parentParameterValueMatcher =
		Pattern.compile(regex).matcher(domainContextModelContent);

	if (parentParameterValueMatcher.find()) {
	    foundMatch = true;
	    wholeContent = parentParameterValueMatcher.group();

	    String foundGroup = parentParameterValueMatcher.group(1);

	    if (add) { // add operation for existent parent parameter
		       // (value)
		wholeContent = wholeContent.replace("}",
			",".concat(parameterValue).concat("}"));
	    } else { // delete operation for existent parent child
		     // parameter (value) relation
		wholeContent = wholeContent.replace(foundGroup, "");
		if (wholeContent.contains("{}")) { // last child
						   // parameter
						   // (value) of
						   // parent
		    wholeContent = "";
		}
	    }
	} else if (add
		&& isCoversRelation) { /**
				        * add operation for non existent parent
				        * parameter value. adding a new
				        * parameter is treated in other method
				        */

	    parentParameterValueMatcher = PARAMETER_HIERARCHY_MARKER_PATTERN
		    .matcher(domainContextModelContent);

	    if (parentParameterValueMatcher.find()) {
		foundMatch = true;
		wholeContent = parentParameterValueMatcher.group();
		wholeContent = wholeContent.concat(System.lineSeparator())
			.concat(parentParameterValue).concat("[covers->{")
			.concat(parameterValue).concat("}].");
	    }
	}

	if (foundMatch) { // finally write replacement to file
	    StringBuffer sb =
		    new StringBuffer(domainContextModelContent.length());
	    parentParameterValueMatcher.appendReplacement(sb,
		    Matcher.quoteReplacement(wholeContent));
	    parentParameterValueMatcher.appendTail(sb);
	    writeChangesToFile(ctxModel.getContextModelFilePath(), sb);
	    return true;
	}

	return false;
    }

    /**
     * Write whole content of StringBuffer to file specified
     * 
     * @param filePath
     * @param sb
     */
    private void writeChangesToFile(String filePath, StringBuffer sb) {
	try {
	    FileWriter writer = new FileWriter(new File(filePath.toString()));
	    writer.write(sb.toString());
	    writer.close();

	    domainContextModelContent = readFileContent(Paths.get(filePath));
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Reads a files´ content and returns it as a String wit white-spaces
     * eliminated
     * 
     * @param filePath
     *            the File content is read from
     * @return content from one File read
     * @throws IOException
     */
    protected static String readFileContent(Path filePath) throws IOException {
	String wholeFileContent = "";

	File file = new File(filePath.toString());
	FileInputStream fis;

	fis = new FileInputStream(file);
	byte[] data = new byte[(int) file.length()];
	fis.read(data);
	fis.close();

	wholeFileContent = new String(data, "UTF-8");

	return wholeFileContent;
    }

    /**
     * Creates the Entity Objects @{link Context} & @{link Rule} parsed
     * from {@paramRef fileContent} and persists them
     * 
     * @param filePath
     *            path to text-file for entity-object {@link Context}
     */
    private Context extractEntitiesFromContextFileContent(String filePath) {

	Matcher ctxMatcher = CONTEXT_PATTERN.matcher(contextContent);

	LocalDateTime now = LocalDateTime.now();
	/**
	 * Create @{link Context} Object with contextName taken from
	 * Regex-Pattern Match
	 **/
	Context context = new Context();
	context.setCreatedAt(now);
	context.setModifiedAt(now);
	context.setFilePath(filePath);

	context.setInstantiatesContextModel(ctxModel);

	if (ctxMatcher.find()) {
	    context.setValue(ctxMatcher.group(1));
	}

	/**
	 * Create @{link Rule} Object with ruleName and ruleContent taken from
	 * Regex-Pattern Match
	 **/
	Matcher ruleMatcher = RULE_PATTERN.matcher(contextContent);

	while (ruleMatcher.find()) {
	    String ruleContent = ruleMatcher.group();
	    String ruleName = ruleMatcher.group(1);

	    Rule rule = new Rule();

	    rule.setCreatedAt(now);
	    rule.setModifiedAt(now);
	    rule.setRuleContent(ruleContent);
	    rule.setRuleName(ruleName);

	    /** Connect Context and Rule **/
	    context.getRules().add(rule);
	    rule.setRelatesTo(context);
	}

	log.info("Context " + context.getValue());
	for (Rule child : context.getRules()) {
	    log.info("rule name: " + child.getRuleName() + " rule content: "
		    + child.getRuleContent());
	}

	return context;
    }
}
