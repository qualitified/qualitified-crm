package org.qualitified.crm.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.MappingNotFoundException;
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
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.bulk.BulkService;
import org.nuxeo.ecm.core.bulk.message.BulkCommand;
import org.nuxeo.ecm.core.bulk.message.BulkStatus;
import org.nuxeo.runtime.api.Framework;
import org.qualitified.crm.service.EmailingService;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Operation(id = BulkSendMailAction.ID, category = Constants.CAT_EXECUTION, label = "BulkSendMailAction", description = "BulkSendMailAction...")
public class BulkSendMailAction {

    public final static String ID = "Qualitified.BulkSendMailAction";
    private Log logger = LogFactory.getLog(BulkSendMailAction.class);


    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession documentManager;


    @OperationMethod
    public void run() throws OperationException, LoginException, JSONException, InterruptedException {
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);
        Map<String, Serializable> statusMail =new HashMap<>();
        statusMail.put("statusMail","Sent");
        BulkCommand command = new BulkCommand.Builder(AutomationBulkAction.ACTION_NAME,
                "SELECT * FROM Interaction WHERE interaction:activity= 'Email' AND ecm:isProxy = 0 AND ecm:isTrashed = 0 AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted' ")
                .repository("default")
                .user("Administrator")
                .param(AutomationBulkAction.OPERATION_ID,"Qualitified.SendMail")
                .param(AutomationBulkAction.OPERATION_PARAMETERS, (Serializable) statusMail)
                .build();

        // run command
        BulkService bulkService = Framework.getService(BulkService.class);
        String commandId = bulkService.submit(command);

        // await end of computation
        bulkService.await(commandId, Duration.ofMinutes(1));

        // get status
        BulkStatus status = bulkService.getStatus(commandId);

    }
}