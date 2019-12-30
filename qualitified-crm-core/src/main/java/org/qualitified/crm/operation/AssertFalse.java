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
@Operation(id= AssertFalse.ID, category= Constants.CAT_EXECUTION, label="AsserFalse", description="")
public class AssertFalse {
    public static final String ID = "Qualitified.AssertFalse";
    private Log logger = LogFactory.getLog(AssertFalse.class);

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "result")
    protected boolean result;

    @OperationMethod
    public void run() throws Exception {
       if(result){
            logger.warn("Test failed: expected [false], result [true].");
        }else{
            logger.warn("Test passed!");
        }
    }

}