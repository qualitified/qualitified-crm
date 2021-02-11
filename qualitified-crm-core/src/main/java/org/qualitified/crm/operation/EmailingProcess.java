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
import org.nuxeo.ecm.automation.core.scripting.DocumentWrapper;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    public void run() throws OperationException, LoginException, JSONException, InterruptedException, ParseException {
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        DocumentModelList campaignDocuments = documentManager
                .query("SELECT * FROM Campaign " +
                        "WHERE campaign:sendDate <= DATE '"+ LocalDateTime.now().format(formatter) +"' " +
                        "AND campaign:status = 'Ready' " +
                        "AND ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0  " +
                        "AND ecm:currentLifeCycleState != 'deleted' " +
                        "AND ecm:isTrashed = 0");
        
        if ( !campaignDocuments.isEmpty() ) {
            for (DocumentModel campaignDoc : campaignDocuments) {
                triggerSendMailBulk(campaignDoc);
            }

        } else logger.error("No campaign found, please check the send date or the status");

    }


    @OperationMethod()
    public void run(DocumentModel campaignDoc) throws OperationException, LoginException, JSONException, InterruptedException, ParseException {
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);
        if (campaignDoc.getPropertyValue("campaign:status").equals("Ready")) {
            triggerSendMailBulk(campaignDoc);
        } else logger.error("This campaign is not ready yet to be sent, please check its status");

    }
    private BulkStatus sendMailBulk(Map<String, Serializable> sendMailBulkParams) throws InterruptedException {

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
    private void triggerSendMailBulk(DocumentModel campaignDoc) throws InterruptedException, ParseException {

        campaignDoc.setPropertyValue("campaign:status", "In Progress");
        documentManager.saveDocument(campaignDoc);
        IdRef automationDocId = new IdRef((String) campaignDoc.getPropertyValue("campaign:automationId"));
        DocumentModel automationDoc = documentManager.getDocument(automationDocId);

        IdRef emailDocId;
        String emailStepZero = (String) automationDoc.getPropertyValue("custom:documentField1");
        String emailStepOne = (String) automationDoc.getPropertyValue("custom:documentField2");
        String emailStepTwo = (String) automationDoc.getPropertyValue("custom:documentField3");

        int waitStepOneAmount = automationDoc.getPropertyValue("custom:integerField1")!= null
                ? Math.toIntExact((long) automationDoc.getPropertyValue("custom:integerField1"))
                : 0 ;
        int waitStepTwoAmount = automationDoc.getPropertyValue("custom:integerField2")!= null
                ? Math.toIntExact((long) automationDoc.getPropertyValue("custom:integerField2"))
                : 0 ;
        int currentStep = campaignDoc.getPropertyValue("custom:integerField10")!= null
                ? Math.toIntExact((long) campaignDoc.getPropertyValue("custom:integerField10"))
                : 0 ;

        GregorianCalendar currentSendDate = (GregorianCalendar) campaignDoc.getPropertyValue("campaign:sendDate");

        switch (currentStep) {
            case 2:
                emailDocId = new IdRef(emailStepTwo);
                stepUpdate(campaignDoc,null,0,currentStep,null,3);
                break;

            case 1:
                emailDocId = new IdRef(emailStepOne);
                stepUpdate(campaignDoc,emailStepTwo,waitStepTwoAmount,currentStep,currentSendDate,2);
                break;

            default :
                emailDocId = new IdRef(emailStepZero);
                stepUpdate(campaignDoc,emailStepOne,waitStepOneAmount,currentStep,currentSendDate,1);
        }

        DocumentModel emailDoc = documentManager.getDocument(emailDocId);

        sendMailBulkParams.put("campaignId", campaignDoc.getId());
        sendMailBulkParams.put("emailId", emailDoc.getId());
        sendMailBulkParams.put("subject", emailDoc.getTitle());
        sendMailBulkParams.put("htmlPart", emailDoc.getPropertyValue("html:content"));

        // run operation that will trigger the send MailBulk
        sendMailBulk(sendMailBulkParams);
    }


    private void stepUpdate(DocumentModel campaignDoc, String emailNextStep, int waitNextStepAmount, int currentStep, GregorianCalendar currentSendDate, int nextStep) throws InterruptedException, ParseException {
        if ( emailNextStep != null ) {
            GregorianCalendar nextSendDate = addDaysCalendar(currentSendDate, waitNextStepAmount);
            campaignDoc.setPropertyValue("custom:integerField10",nextStep);
            campaignDoc.setPropertyValue("custom:stringField1","Step"+nextStep);
            campaignDoc.setPropertyValue("campaign:sendDate", nextSendDate);
            campaignDoc.setPropertyValue("campaign:status", "Ready");

        } else {
           /* campaignDoc.setPropertyValue("custom:integerField10",currentStep);
            campaignDoc.setPropertyValue("custom:stringField1","Step"+currentStep);*/
            campaignDoc.setPropertyValue("campaign:status", "Sent");
        }
        documentManager.saveDocument(campaignDoc);
    }

    private static GregorianCalendar addDaysCalendar(GregorianCalendar date, int amount) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        date.add(GregorianCalendar.DATE,amount);
        return date;
    }
}
