package org.qualitified.crm.operation;

import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.turbomanage.httpclient.json.JsonResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.api.Framework;
import org.qualitified.crm.service.EmailingService;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Operation(id = SendMail.ID, category = Constants.CAT_EXECUTION, label = "SendMail", description = "SendMail...")
public class SendMail {
    public final static String ID = "Qualitified.SendMail";
    @Context
    protected OperationContext ctx;
    @Context
    protected CoreSession documentManager;
    @Param(name = "campaignId")
    protected String campaignId;
    @Param(name = "emailId")
    protected String emailId;
    @Param(name = "subject")
    protected String mailSubject;
    @Param(name = "htmlPart")
    protected String mailHtmlPart;

    private Log logger = LogFactory.getLog(SendMail.class);
    private String fromEmail=Framework.getProperty("mailjetService.fromEmail");
    private String fromName=Framework.getProperty("mailjetService.fromName");
    private String nuxeoUrl = Framework.getProperty("nuxeo.url");

    private String MessageID;
    private Map<String, Object> mailDetails = new HashMap<String, Object>();
    private ArrayList<String> contacts = new ArrayList<>();


    @OperationMethod
    public void run(DocumentModel contactDoc) throws OperationException, LoginException, JSONException, MailjetSocketTimeoutException {
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);

        EmailingService emailingService = Framework.getService(EmailingService.class);

        String firstName = (contactDoc.getPropertyValue("person:firstName")!= null)
                ? (String) contactDoc.getPropertyValue("person:firstName")
                : "";
        String lastName = (contactDoc.getPropertyValue("person:lastName")!= null)
                ? contactDoc.getPropertyValue("person:lastName") + " "
                : "";
        String toName = lastName + firstName;
        String toEmail = (String) contactDoc.getPropertyValue("person:email");
        String unsubLink = nuxeoUrl+"/site/api/v1/unsub/"+contactDoc.getId();
        String htmlPart = mailHtmlPart.contains("$$unsubLink")
                ? mailHtmlPart.replace("$$unsubLink",unsubLink)
                : mailHtmlPart;

        mailDetails.put("fromEmail",fromEmail);
        mailDetails.put("fromName",fromName);
        mailDetails.put("toEmail",toEmail);
        mailDetails.put("toName",toName);
        mailDetails.put("subject",mailSubject);
        mailDetails.put("textPart","");
        mailDetails.put("htmlPart",htmlPart);

        try {
            MailjetResponse response = emailingService.send(mailDetails);

             if (response.getStatus() != 401) {

                 if (response.getJSONArray("Messages").getJSONObject(0).getString("Status").equals("success")) {

                     MessageID = Long.toString(response.getJSONArray("Messages").getJSONObject(0).getJSONArray("To")
                             .getJSONObject(0).getLong("MessageID"));

                     DocumentModel interactionDoc = documentManager.createDocumentModel("/Marketing", "interaction", "Interaction");
                     interactionDoc.setPropertyValue("dc:title", "Mail to "+ contactDoc.getTitle());
                     interactionDoc.setPropertyValue("interaction:status", "DONE");
                     interactionDoc.setPropertyValue("interaction:activity", "Emailing");
                     interactionDoc.setPropertyValue("interaction:date", Calendar.getInstance());
                     interactionDoc.setPropertyValue("interaction:campaignId",campaignId);
                     interactionDoc.setPropertyValue("interaction:messageID",MessageID);
                     interactionDoc.setPropertyValue("custom:documentField10",emailId);
                     interactionDoc.setPropertyValue("interaction:isSent", 1);

                     contacts.add(contactDoc.getId());
                     interactionDoc.setPropertyValue("interaction:contact",contacts);
                     documentManager.createDocument(interactionDoc);

                     logger.trace("Message with subject " +mailSubject+ "sent to " +toEmail+ "from" +fromEmail);

                 } else {
                     logger.error(response.getJSONArray("Messages").getJSONObject(0).getJSONArray("Errors")
                         .getJSONObject(0).getString("ErrorMessage"));
                 }

             } else {
                 logger.error(response.getString("ErrorMessage"));
             }


        } catch (MailjetException m) {
            logger.error("Error while running mailjet service", m);
        }
    }
}
