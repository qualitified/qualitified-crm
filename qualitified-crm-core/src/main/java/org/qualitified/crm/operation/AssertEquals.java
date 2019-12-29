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
 * Created by michaelgena on 28/11/2019.
 */
@Operation(id= AssertEquals.ID, category= Constants.CAT_EXECUTION, label="AssertEquals", description="")
public class AssertEquals {
    public static final String ID = "Qualitified.AssertEquals";
    private Log logger = LogFactory.getLog(AssertEquals.class);

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "expected")
    protected String expected;

    @Param(name = "result")
    protected String result;

    @Param(name = "message")
    protected String message;

    @OperationMethod
    public void run() throws Exception {
        if(expected == null){
            logger.error("Test failed: expected value is null.");
        }else if(result == null){
            logger.error("Test failed: result value is null.");
        }else if(!expected.equals(result)) {
            logger.error("Test failed: "+message+", expected ["+expected+"], result ["+result+"]");
        }else{
            logger.warn("Test passed!");
        }
    }

}