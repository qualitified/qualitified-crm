package org.qualitified.crm.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.operations.services.bulk.AutomationBulkAction;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.bulk.BulkService;
import org.nuxeo.ecm.core.bulk.message.BulkCommand;
import org.nuxeo.ecm.core.bulk.message.BulkStatus;
import org.nuxeo.runtime.api.Framework;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Operation(id = EmailingHistoryProcess.ID, category = Constants.CAT_EXECUTION, label = "EmailingHistoryProcess", description = "EmailingHistoryProcess...")
public class EmailingHistoryProcess {

    public final static String ID = "Qualitified.EmailingHistoryProcess";
    private Log logger = LogFactory.getLog(EmailingHistoryProcess.class);


    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession documentManager;

    Map<String, Serializable> fetchHistoryParam =new HashMap<>();
    @OperationMethod
    public void run() throws OperationException, LoginException, JSONException, InterruptedException {
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);
        fetchHistoryParam.put("Status","");
        BulkCommand fetchMailHistoryCommand = new BulkCommand.Builder(AutomationBulkAction.ACTION_NAME,
                "SELECT * FROM Interaction WHERE interaction:activity= 'Emailing' " +
                        "AND interaction:status= 'DONE' " +
                        "AND ecm:isProxy = 0 AND ecm:isTrashed = 0 AND ecm:isCheckedInVersion = 0 " +
                        "AND ecm:currentLifeCycleState != 'deleted' ")
                .repository("default")
                .user("Administrator")
                .param(AutomationBulkAction.OPERATION_ID, "Qualitified.FetchMailHistory")
                .param(AutomationBulkAction.OPERATION_PARAMETERS, (Serializable) fetchHistoryParam)
                .build();

        // run command
        BulkService fetchMailHistoryBulkService = Framework.getService(BulkService.class);
        String fetchMailHistoryCommandId = fetchMailHistoryBulkService.submit(fetchMailHistoryCommand);
        logger.warn("fetchMailHistoryBulk running with commandId '"+fetchMailHistoryCommandId+"'");
        // await end of computation
        //fetchMailHistoryBulkService.await(fetchMailHistoryCommandId, Duration.ofMinutes(1));

        // get status
        BulkStatus fetchMailHistoryBulkStatus = fetchMailHistoryBulkService.getStatus(fetchMailHistoryCommandId);

    }
    @OperationMethod()
    public void run(DocumentModel campaignDoc) throws OperationException, LoginException, JSONException, InterruptedException {
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);
        fetchHistoryParam.put("Status","");
        BulkCommand fetchMailHistoryCommand = new BulkCommand.Builder(AutomationBulkAction.ACTION_NAME,
                "SELECT * FROM Interaction WHERE interaction:campaignId= '"+ campaignDoc.getId()+ "' " +
                        "AND interaction:activity= 'Emailing' " +
                        "AND interaction:status= 'DONE' " +
                        "AND ecm:isProxy = 0 AND ecm:isTrashed = 0 AND ecm:isCheckedInVersion = 0 " +
                        "AND ecm:currentLifeCycleState != 'deleted' ")
                .repository("default")
                .user("Administrator")
                .param(AutomationBulkAction.OPERATION_ID, "Qualitified.FetchMailHistory")
                .param(AutomationBulkAction.OPERATION_PARAMETERS, (Serializable) fetchHistoryParam)
                .build();

        // run command
        BulkService fetchMailHistoryBulkService = Framework.getService(BulkService.class);
        String fetchMailHistoryCommandId = fetchMailHistoryBulkService.submit(fetchMailHistoryCommand);
        logger.warn("fetchMailHistoryBulk running with commandId '"+fetchMailHistoryCommandId+"'");

        // await end of computation
        // fetchMailHistoryBulkService.await(fetchMailHistoryCommandId, Duration.ofMinutes(1));

        // get status
        BulkStatus fetchMailHistoryBulkStatus = fetchMailHistoryBulkService.getStatus(fetchMailHistoryCommandId);

    }
}
