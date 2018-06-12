package dke.cbrm.persistence.parser;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dke.cbrm.cli.CBRInterface;
import dke.cbrm.persistence.model.ParentChildRelation;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * @author ahauer
 *
 *         FloraCommandOutputParser´s responsibility is to parse the
 *         shell- / console-output from Commands "getCtxHierarchy",
 *         "getParameters" and "getParameterValuesHiearchy"
 *         into @{link Parameter} and @{link Context}
 *         object-representation, which can be modfied by @{link
 *         ModificationOperation} and then updated into @{link
 *         CbrmRepository}
 */
@Data
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FloraCommandOutputParser {

    private final CBRInterface fl;

    // public void runOutputParser() {
    // buildContextHierarchy();
    // buildParameterHierarchy();
    // }

    

}
