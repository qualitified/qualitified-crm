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
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.operations.services.bulk.AutomationBulkAction;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
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

@Operation(id = SendMailBulk.ID, category = Constants.CAT_EXECUTION, label = "SendMailBulk", description = "SendMailBulk...")

public class SendMailBulk {
    public final static String ID = "Qualitified.SendMailBulk";
    private Log logger = LogFactory.getLog(SendMailBulk.class);


    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession documentManager;

    @Param(name = "campaignId")
    protected String campaignId;

    @Param(name = "subject")
    protected String subject;

    @Param(name = "htmlPart")
    protected String htmlPart;

    @OperationMethod
    public BulkStatus run() throws OperationException, LoginException, JSONException, InterruptedException {
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);
        Map<String, Serializable> mailContentParam =new HashMap<>();
        mailContentParam.put("subject", subject);
        mailContentParam.put("htmlPart", htmlPart);

        BulkCommand sendMailCommand = new BulkCommand.Builder(AutomationBulkAction.ACTION_NAME,
                "SELECT * FROM Interaction WHERE interaction:activity= 'Emailing' " +
                        "AND interaction:status= 'TODO' AND interaction:campaignId ='"+campaignId+"' " +
                        "AND ecm:isProxy = 0 AND ecm:isTrashed = 0 AND ecm:isCheckedInVersion = 0 " +
                        "AND ecm:currentLifeCycleState != 'deleted' ")
                .repository("default")
                .user("Administrator")
                .param(AutomationBulkAction.OPERATION_ID, "Qualitified.AutomationSendMail")
                .param(AutomationBulkAction.OPERATION_PARAMETERS, (Serializable) mailContentParam)
                .build();

        // run command
        BulkService sendMailBulkService = Framework.getService(BulkService.class);
        String sendMailCommandId = sendMailBulkService.submit(sendMailCommand);

        // await end of computation
        // sendMailBulkService.await(sendMailCommandId, Duration.ofMinutes(1));

        // get status
        BulkStatus sendMailBulkStatus = sendMailBulkService.getStatus(sendMailCommandId);
        return sendMailBulkStatus;
    }
}
