package org.qualitified.crm.operation;

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
import org.nuxeo.ecm.core.api.*;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import org.nuxeo.runtime.api.Framework;
import org.qualitified.crm.service.EmailingService;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Operation(id = SendMail.ID, category = Constants.CAT_EXECUTION, label = "SendMail", description = "SendMail...")
public class SendMail {

    public final static String ID = "Qualitified.SendMail";
    @Context
    protected OperationContext ctx;
    @Context
    protected CoreSession documentManager;
    @Param(name = "statusMail")
    protected String statusMail;

    private Log logger = LogFactory.getLog(SendMail.class);
    private String MessageID;
    private Map<String, Object> mailDetails = new HashMap<String, Object>();


    @OperationMethod
    public void run(DocumentModel interactionDoc) throws OperationException, LoginException, JSONException, MailjetSocketTimeoutException {
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);

        EmailingService emailingService = Framework.getService(EmailingService.class);
        IdRef idRef = new IdRef((String) interactionDoc.getPropertyValue("interaction:campaignId"));
        DocumentModel campaignDoc = (idRef.reference()!= null)
                                  ? documentManager.getDocument(idRef)
                                  : documentManager.getDocument(new PathRef("/New marketing/mail campaign"));

        String fromEmail = (interactionDoc.getPropertyValue("interaction:fromEmail") != null)
                         ? (String) interactionDoc.getPropertyValue("interaction:fromEmail")
                         : "fs.bilel@gmail.com";
        mailDetails.put("fromEmail",fromEmail);
        String fromName = (interactionDoc.getPropertyValue("interaction:fromName") != null)
                        ? (String) interactionDoc.getPropertyValue("interaction:fromName")
                        : "Fatnassi BILEL";
        mailDetails.put("fromName",fromName);
        String toEmail = (interactionDoc.getPropertyValue("interaction:toEmail") != null)
                       ? (String) interactionDoc.getPropertyValue("interaction:toEmail")
                       : "bfatnassi@qualitified.com";
        mailDetails.put("toEmail",toEmail);
        String toName = (interactionDoc.getPropertyValue("interaction:toName") != null)
                      ? (String) interactionDoc.getPropertyValue("interaction:toName")
                      : "BFatnassi";
        mailDetails.put("toName",toName);
        String subject = (campaignDoc.getPropertyValue("campaign:subject") != null)
                       ? (String) campaignDoc.getPropertyValue("campaign:subject")
                       : "Mail from qualitified";
        mailDetails.put("subject",subject);
        String textPart = (campaignDoc.getPropertyValue("campaign:textPart") != null)
                        ? (String) campaignDoc.getPropertyValue("campaign:textPart")
                        : "This is a replacement Mail for interaction documents with empty mail details";
        mailDetails.put("textPart",textPart);
        String htmlPart = (campaignDoc.getPropertyValue("campaign:htmlPart") != null)
                        ? (String) campaignDoc.getPropertyValue("campaign:htmlPart")
                        : "<h3>Please check details in interaction documents...</h3>";
        mailDetails.put("htmlPart",htmlPart);

        try {
            JSONArray response = emailingService.send(mailDetails);
            MessageID = Long.toString(response.getJSONObject(0).getJSONArray("To")
                    .getJSONObject(0).getLong("MessageID"));
        } catch (MailjetException e) {
            logger.error("Error while running mailjet service", e);
        } finally {
            interactionDoc.setPropertyValue("interaction:statusMail", statusMail);
            interactionDoc.setPropertyValue("interaction:messageID", MessageID);
            documentManager.saveDocument(interactionDoc);
        }


    }

}
