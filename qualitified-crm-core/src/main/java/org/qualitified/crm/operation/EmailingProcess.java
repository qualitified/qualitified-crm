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
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.bulk.BulkService;
import org.nuxeo.ecm.core.bulk.message.BulkCommand;
import org.nuxeo.ecm.core.bulk.message.BulkStatus;
import org.nuxeo.runtime.api.Framework;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.nuxeo.ecm.automation.core.operations.blob.RunConverter.log;

@Operation(id = EmailingProcess.ID, category = Constants.CAT_EXECUTION, label = "EmailingProcess", description = "EmailingProcess...")
public class EmailingProcess {

    public final static String ID = "Qualitified.EmailingProcess";
    private Log logger = LogFactory.getLog(EmailingProcess.class);


    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession documentManager;


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

                BulkStatus prepareMailBulkStatus = prepareMailBulk(campaignDoc);

                if (prepareMailBulkStatus.isCompleted()) {
                    logger.info("Interactions have been created for all '"+ campaignDoc.getTitle()+"' contacts ");
                    BulkStatus sendMailBulkStatus = sendMailBulk(campaignDoc);
                    campaignDoc.setPropertyValue("campaign:status","In Progress");
                    documentManager.saveDocument(campaignDoc);

                    if (sendMailBulkStatus.isCompleted()) {
                        logger.info("Emails have been sent for all '"+ campaignDoc.getTitle()+"' contacts ");
                        campaignDoc.setPropertyValue("campaign:status","Sent");                    documentManager.saveDocument(campaignDoc);
                        documentManager.saveDocument(campaignDoc);
                    } else logger.error("Something wrong with the sendMailBulk");

                } else logger.error("Something wrong with the prepareMailBulk");
            }

        } else logger.error("No campaign found, please check the send date or the status");

    }

    private BulkStatus sendMailBulk(DocumentModel campaignDoc) throws InterruptedException {
        Map<String, Serializable> mailContentParam =new HashMap<>();

        IdRef automationDocId = new IdRef((String) campaignDoc.getPropertyValue("campaign:automationId"));
        DocumentModel automationDoc = documentManager.getDocument(automationDocId);
        IdRef emailDocId =new IdRef((String) automationDoc.getPropertyValue("custom:documentField1"));
        DocumentModel emailDoc = documentManager.getDocument(emailDocId);

        mailContentParam.put("subject", emailDoc.getTitle());
        mailContentParam.put("htmlPart", emailDoc.getPropertyValue("html:content"));
        BulkCommand sendMailCommand = new BulkCommand.Builder(AutomationBulkAction.ACTION_NAME,
                "SELECT * FROM Interaction WHERE interaction:activity= 'Emailing' " +
                        "AND interaction:status= 'TODO' AND interaction:campaignId ='"+ campaignDoc.getId()+"' " +
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
        sendMailBulkService.await(sendMailCommandId, Duration.ofMinutes(1));

        // get status
        BulkStatus sendMailBulkStatus = sendMailBulkService.getStatus(sendMailCommandId);
        return sendMailBulkStatus;
    }

    private BulkStatus prepareMailBulk(DocumentModel campaignDoc) throws InterruptedException {
        Map<String, Serializable> campaignIdParam = new HashMap<>();
        campaignIdParam.put("campaignId", campaignDoc.getId());

        BulkCommand prepareMailCommand = new BulkCommand.Builder(AutomationBulkAction.ACTION_NAME,
                "SELECT * FROM Document " +
                        "WHERE ecm:primaryType IN ('Contact','Silhouette')" +
                        "AND ecm:mixinType != 'HiddenInNavigation'" +
                        "AND ecm:isCheckedInVersion = 0 " +
                        "AND ecm:currentLifeCycleState !='deleted'" +
                        "AND ecm:isTrashed = 0 " +
                        "AND collectionMember:collectionIds/* = '"+campaignDoc.getId()+"' ")
                .repository("default")
                .user("Administrator")
                .param(AutomationBulkAction.OPERATION_ID,"Qualitified.CreateInteractions")
                .param(AutomationBulkAction.OPERATION_PARAMETERS, (Serializable) campaignIdParam)
                .build();

        // run command
        BulkService prepareMailBulkService = Framework.getService(BulkService.class);
        String prepareMailCommandId = prepareMailBulkService.submit(prepareMailCommand);

        // await end of computation
        prepareMailBulkService.await(prepareMailCommandId, Duration.ofMinutes(1));

        // get status
        BulkStatus prepareMailBulkStatus = prepareMailBulkService.getStatus(prepareMailCommandId);
        return prepareMailBulkStatus;
    }
}