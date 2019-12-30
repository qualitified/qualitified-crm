package org.qualitified.crm.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;

/**
 * Created by michaelgena on 29/12/2019.
 */
@Operation(id= AssertTrue.ID, category= Constants.CAT_EXECUTION, label="AsserTrue", description="")
public class AssertTrue {
    public static final String ID = "Qualitified.AssertTrue";
    private Log logger = LogFactory.getLog(AssertTrue.class);

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "result")
    protected boolean result;

    @OperationMethod
    public void run() throws Exception {
        if(!result){
            logger.warn("Test failed: expected [true], result [false].");
        }else{
            logger.warn("Test passed!");
        }
    }

}