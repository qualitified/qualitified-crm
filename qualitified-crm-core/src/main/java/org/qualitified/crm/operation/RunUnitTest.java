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
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.transaction.TransactionHelper;

import javax.sql.rowset.spi.TransactionalWriter;
import java.util.*;

/**
 * Created by michaelgena on 28/11/2019.
 */
@Operation(id= RunUnitTest.ID, category= Constants.CAT_EXECUTION, label="RunUnitTest", description="")
public class RunUnitTest {
    public static final String ID = "Qualitified.RunUnitTest";
    private Log logger = LogFactory.getLog(RunUnitTest.class);

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @OperationMethod
    public void run(DocumentModel unitTest) throws Exception {
        logger.warn("*****************************");
        logger.warn("Starting to Run Unit Test...");
        logger.warn("*****************************");
        OperationContext operationContext = new OperationContext(session);
        Map<String, Object> params = new HashMap<>();
        String scriptNote = (String) unitTest.getPropertyValue("note:note");
        params.put("script", scriptNote);
        logger.warn("Running Unit Test ["+unitTest.getTitle()+"]...");
        AutomationService automationService = Framework.getService(AutomationService.class);
        operationContext.put("scriptId",unitTest.getId());

        DocumentModel scriptDocument = session.getDocument(unitTest.getRef());
        scriptDocument.setPropertyValue("dc:description", null);
        scriptDocument.setPropertyValue("scriptnote:isValid", null);
        session.saveDocument(scriptDocument);
        session.save();
        String log = "";
        try {
            automationService.runInNewTx(operationContext, "javascript.runUnitTest", params, 30, true);
            //logger.warn("Unit test ["+unitTest.getTitle()+"] passed!");
        } catch (OperationException e) {
            logger.error("Error while running unit test ["+unitTest.getTitle()+"]", e);
            logger.warn("Aborting Unit Tests.");
            log += Calendar.getInstance(TimeZone.getDefault()).getTime()+" Error while running unit test\n";
            log += e.getMessage();
            //throw (new NuxeoException(e));
        }
        DocumentModel scriptRunDocument = session.getDocument(unitTest.getRef());
        if(!log.equals("")) {
            log += scriptRunDocument.getPropertyValue("dc:description") != null ? (String) scriptRunDocument.getPropertyValue("dc:description") : "";
            scriptRunDocument.setPropertyValue("dc:description", log);
            scriptRunDocument.setPropertyValue("scriptnote:isValid", false);
            session.saveDocument(scriptRunDocument);
            session.save();
        }else if(scriptRunDocument.getPropertyValue("dc:description") == null || ((String)scriptRunDocument.getPropertyValue("dc:description")).equals("")) {
            scriptRunDocument.setPropertyValue("scriptnote:isValid", true);
            session.saveDocument(scriptRunDocument);
            session.save();
            logger.warn("Test passed!");
        }

        logger.warn("*****************************");
        logger.warn("Done Running Unit Test.");
        logger.warn("*****************************");
    }

}