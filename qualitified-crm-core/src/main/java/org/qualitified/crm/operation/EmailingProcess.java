package org.qualitified.crm.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.h2.util.StringUtils;
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
    String contactsQuery;



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
    private BulkStatus sendMailBulk(Map<String, Serializable> sendMailBulkParams, String contactsQuery) throws InterruptedException {
        if (contactsQuery!= null) {
            BulkCommand prepareMailCommand = new BulkCommand.Builder(AutomationBulkAction.ACTION_NAME, contactsQuery)
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
        return null;
    }
    private void triggerSendMailBulk(DocumentModel campaignDoc) throws InterruptedException, ParseException {

        campaignDoc.setPropertyValue("campaign:status", "In Progress");
        documentManager.saveDocument(campaignDoc);
        IdRef automationDocId = new IdRef((String) campaignDoc.getPropertyValue("campaign:automationId"));
        DocumentModel automationDoc = documentManager.getDocument(automationDocId);
        IdRef emailDocId;
        DocumentModel emailDoc;
        String emailStepZero = (String) automationDoc.getPropertyValue("custom:documentField1");
        String emailStepOne = (String) automationDoc.getPropertyValue("custom:documentField2");
        String emailStepTwo = (String) automationDoc.getPropertyValue("custom:documentField3");
        String emailStepThree = (String) automationDoc.getPropertyValue("custom:documentField4");

        int waitStepOneAmount = automationDoc.getPropertyValue("custom:integerField1")!= null
                ? Math.toIntExact((long) automationDoc.getPropertyValue("custom:integerField1"))
                : 0 ;
        int waitStepTwoAmount = automationDoc.getPropertyValue("custom:integerField2")!= null
                ? Math.toIntExact((long) automationDoc.getPropertyValue("custom:integerField2"))
                : 0 ;
        int waitStepThreeAmount = automationDoc.getPropertyValue("custom:integerField3")!= null
                ? Math.toIntExact((long) automationDoc.getPropertyValue("custom:integerField3"))
                : 0 ;
        int currentStep = campaignDoc.getPropertyValue("custom:integerField10")!= null
                ? Math.toIntExact((long) campaignDoc.getPropertyValue("custom:integerField10"))
                : 0 ;
        GregorianCalendar currentSendDate = (GregorianCalendar) campaignDoc.getPropertyValue("campaign:sendDate");
        switch (currentStep) {
            case 3:
                emailDocId = new IdRef(emailStepThree);
                setContactsQuery(campaignDoc.getId(), emailStepTwo, true);
                stepUpdate(campaignDoc,contactsQuery,emailStepTwo,emailStepThree,0,2,currentSendDate,0);
                break;

            case 2:
                emailDocId = new IdRef(emailStepTwo);
                setContactsQuery(campaignDoc.getId(), emailStepOne, true);
                stepUpdate(campaignDoc,contactsQuery,emailStepOne,emailStepThree,waitStepThreeAmount,1,currentSendDate,3);
                break;

            case 1:
                emailDocId = new IdRef(emailStepOne);
                setContactsQuery(campaignDoc.getId(), emailStepZero, false);
                stepUpdate(campaignDoc,contactsQuery,emailStepZero,emailStepTwo,waitStepTwoAmount,0,currentSendDate,2);
                break;

            default :
                emailDocId = new IdRef(emailStepZero);
                setContactsQuery(campaignDoc.getId(), null, false);
                stepUpdate(campaignDoc,contactsQuery,emailStepZero,emailStepOne,waitStepOneAmount,currentStep,currentSendDate,1);
        }

        emailDoc = documentManager.getDocument(emailDocId);

        sendMailBulkParams.put("campaignId", campaignDoc.getId());
        sendMailBulkParams.put("emailId", emailDoc.getId());
        sendMailBulkParams.put("subject", emailDoc.getTitle());
        sendMailBulkParams.put("htmlPart", emailDoc.getPropertyValue("html:content"));

        // run operation that will trigger the send MailBulk
        sendMailBulk(sendMailBulkParams, contactsQuery);
    }


    private void stepUpdate(DocumentModel campaignDoc, String contactsQuery, String emailPreviousStep, String emailNextStep, int waitNextStepAmount, int previousStep, GregorianCalendar currentSendDate, int nextStep) throws InterruptedException, ParseException {
        if ( emailNextStep != null && contactsQuery != null ) {
            Date nextSendDate = addDaysCalendar(currentSendDate, waitNextStepAmount);
            campaignDoc.setPropertyValue("campaign:sendDate", nextSendDate);
            campaignDoc.setPropertyValue("custom:integerField10",nextStep);
            campaignDoc.setPropertyValue("custom:documentField9",emailNextStep);
            if ( nextStep == 0 ) {
                campaignDoc.setPropertyValue("campaign:status", "Sent");

            } else {
                campaignDoc.setPropertyValue("campaign:status", "Ready");
            }

        } else {
                campaignDoc.setPropertyValue("custom:integerField10",previousStep);
                campaignDoc.setPropertyValue("custom:documentField9",emailPreviousStep);
                campaignDoc.setPropertyValue("campaign:status", "Sent");

        }
        documentManager.saveDocument(campaignDoc);
    }

    private static Date addDaysCalendar(GregorianCalendar date, int amount) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        GregorianCalendar newDate = new GregorianCalendar();
        newDate.setTime(date.getTime());
        newDate.add(GregorianCalendar.DATE,amount);
        return newDate.getTime();
    }

    private String setContactsQuery(String campaignId, String emailId, Boolean doFilter) {
        String activeContacts;
        if ( doFilter == true ){
            DocumentModelList interactionDocuments = documentManager
                    .query("SELECT * FROM Interaction WHERE interaction:campaignId= '"+ campaignId+ "' " +
                            "AND custom:documentField10= '"+ emailId+ "' " +
                            "AND interaction:isOpened = 1 " +
                            "AND interaction:activity= 'Emailing' " +
                            "AND interaction:status= 'DONE' " +
                            "AND interaction:messageID IS NOT NULL " +
                            "AND ecm:isProxy = 0 AND ecm:isTrashed = 0 AND ecm:isCheckedInVersion = 0 " +
                            "AND ecm:currentLifeCycleState != 'deleted' ");
            ArrayList<String> personList = new ArrayList<>();

            if ( !interactionDocuments.isEmpty() ) {
                for (DocumentModel interactionDoc : interactionDocuments) {
                    String[] person = (String[]) interactionDoc.getPropertyValue("interaction:contact");
                    String formattedPerson ="'"+person[0]+"'";
                    personList.add(formattedPerson);
                }
                activeContacts = personList.toString().replace("[","(").replace("]",")");
                contactsQuery = "SELECT * FROM Document " +
                        "WHERE ecm:primaryType IN ('Contact','Silhouette')" +
                        "AND ecm:uuid IN " + activeContacts + " " +
                        "AND custom:integerField1 = 0 " +
                        "AND ecm:mixinType != 'HiddenInNavigation'" +
                        "AND ecm:isCheckedInVersion = 0 " +
                        "AND ecm:currentLifeCycleState !='deleted'" +
                        "AND ecm:isTrashed = 0 " +
                        "AND collectionMember:collectionIds/* = '" + campaignId + "' ";
            } else {
                contactsQuery = null;
            }
            return contactsQuery;

        } else {
            contactsQuery = "SELECT * FROM Document " +
                    "WHERE ecm:primaryType IN ('Contact','Silhouette')" +
                    "AND (custom:integerField1 IS NULL OR custom:integerField1 = 0) " +
                    "AND ecm:mixinType != 'HiddenInNavigation'" +
                    "AND ecm:isCheckedInVersion = 0 " +
                    "AND ecm:currentLifeCycleState !='deleted'" +
                    "AND ecm:isTrashed = 0 " +
                    "AND collectionMember:collectionIds/* = '" + campaignId + "' ";
        }

        return contactsQuery;
    }
}
