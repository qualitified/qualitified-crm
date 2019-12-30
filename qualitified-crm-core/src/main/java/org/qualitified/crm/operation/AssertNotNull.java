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
@Operation(id= AssertNotNull.ID, category= Constants.CAT_EXECUTION, label="AsserNottNull", description="")
public class AssertNotNull {
    public static final String ID = "Qualitified.AssertNotNull";
    private Log logger = LogFactory.getLog(AssertNotNull.class);

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "result")
    protected Object result;

    @OperationMethod
    public void run() throws Exception {
        if(result == null){
            logger.error("Test failed: expected [Not null], value [null].");
        }else{
            logger.warn("Test passed!");
        }
    }

}