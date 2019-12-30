package org.qualitified.crm.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.runtime.api.Framework;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by michaelgena on 28/11/2019.
 */
@Operation(id= RunUnitTests.ID, category= Constants.CAT_EXECUTION, label="RunUnitTests", description="")
public class RunUnitTests {
    public static final String ID = "Qualitified.RunUnitTests";
    private Log logger = LogFactory.getLog(RunUnitTests.class);

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @OperationMethod
    public void run() throws Exception {
        List<DocumentModel> unitTests = CoreInstance.doPrivileged(session, (session) -> {
            return session.query("SELECT * FROM ScriptNote WHERE ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:isTrashed = 0 AND ecm:currentLifeCycleState != 'deleted' AND dc:title ILIKE 'UnitTest%' ORDER BY scriptnote:order ASC, dc:created ASC");
        });
        logger.warn("*****************************");
        logger.warn("Starting to Run Unit Tests...");
        logger.warn("*****************************");
        for(DocumentModel unitTest : unitTests) {
            OperationContext operationContext = new OperationContext(session);
            Map<String, Object> params = new HashMap<>();
            String scriptNote = (String) unitTest.getPropertyValue("note:note");
            params.put("script", scriptNote);
            logger.warn("Running Unit Test ["+unitTest.getTitle()+"]...");
            AutomationService automationService = Framework.getService(AutomationService.class);
            try {
                automationService.run(operationContext, "javascript.runUnitTest", params);
                //logger.warn("Unit test ["+unitTest.getTitle()+"] passed!");
            } catch (OperationException e) {
                logger.error("Error while running unit test ["+unitTest.getTitle()+"]", e);
                logger.warn("Aborting Unit Tests.");
                throw (new NuxeoException(e));
            }
        }
        logger.warn("*****************************");
        logger.warn("Done Running Unit Tests.");
        logger.warn("*****************************");
    }

}