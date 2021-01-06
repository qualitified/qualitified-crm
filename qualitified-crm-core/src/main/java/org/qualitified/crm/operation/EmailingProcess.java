package org.qualitified.crm.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.operations.services.bulk.AutomationBulkAction;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.bulk.BulkService;
import org.nuxeo.ecm.core.bulk.message.BulkCommand;
import org.nuxeo.ecm.core.bulk.message.BulkStatus;
import org.nuxeo.runtime.api.Framework;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Operation(id = EmailingProcess.ID, category = Constants.CAT_EXECUTION, label = "EmailingProcess", description = "EmailingProcess...")
public class EmailingProcess {

    public final static String ID = "Qualitified.EmailingProcess";
    private Log logger = LogFactory.getLog(EmailingProcess.class);


    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession documentManager;

    Map<String, Serializable> sendMailBulkParams =new HashMap<>();

    @OperationMethod
    public void run() throws OperationException, LoginException, JSONException, InterruptedException {
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        DocumentModelList campaignDocuments = documentManager
                .query("SELECT * FROM Campaign " +
                        "WHERE campaign:sendDate <=  '"+ LocalDateTime.now().format(formatter) +"' " +
                        "AND campaign:status = 'Ready' " +
                        "AND ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0  " +
                        "AND ecm:currentLifeCycleState != 'deleted' " +
                        "AND ecm:isTrashed = 0");
        
        if ( !campaignDocuments.isEmpty() ) {
            for (DocumentModel campaignDoc : campaignDocuments) {

                campaignDoc.setPropertyValue("campaign:status","In ");
                documentManager.saveDocument(campaignDoc);
                IdRef automationDocId = new IdRef((String) campaignDoc.getPropertyValue("campaign:automationId"));
                DocumentModel automationDoc = documentManager.getDocument(automationDocId);
                IdRef emailDocId = new IdRef((String) automationDoc.getPropertyValue("custom:documentField1"));
                DocumentModel emailDoc = documentManager.getDocument(emailDocId);

                sendMailBulkParams.put("campaignId",campaignDoc.getId());
                sendMailBulkParams.put("subject", emailDoc.getTitle());
                sendMailBulkParams.put("htmlPart", emailDoc.getPropertyValue("html:content"));
                // run operation that will trigger the PrepareMailBulk
                prepareMailBulk(sendMailBulkParams);
                campaignDoc.setPropertyValue("campaign:status","Sent");
                documentManager.saveDocument(campaignDoc);
            }

        } else logger.error("No campaign found, please check the send date or the status");

    }

    private BulkStatus prepareMailBulk(Map<String, Serializable> sendMailBulkParams) throws InterruptedException {

        BulkCommand prepareMailCommand = new BulkCommand.Builder(AutomationBulkAction.ACTION_NAME,
                "SELECT * FROM Document " +
                        "WHERE ecm:primaryType IN ('Contact','Silhouette')" +
                        "AND ecm:mixinType != 'HiddenInNavigation'" +
                        "AND ecm:isCheckedInVersion = 0 " +
                        "AND ecm:currentLifeCycleState !='deleted'" +
                        "AND ecm:isTrashed = 0 " +
                        "AND collectionMember:collectionIds/* = '"+sendMailBulkParams.get("campaignId")+"' ")
                .repository("default")
                .user("Administrator")
                .param(AutomationBulkAction.OPERATION_ID,"Qualitified.SendMail")
                .param(AutomationBulkAction.OPERATION_PARAMETERS, (Serializable) sendMailBulkParams)
                .build();

        // run command
        BulkService prepareMailBulkService = Framework.getService(BulkService.class);
        String prepareMailCommandId = prepareMailBulkService.submit(prepareMailCommand);

        // await end of computation
        //prepareMailBulkService.await(prepareMailCommandId, Duration.ofMinutes(1));

        // get status
        BulkStatus prepareMailBulkStatus = prepareMailBulkService.getStatus(prepareMailCommandId);
        return prepareMailBulkStatus;
    }
}