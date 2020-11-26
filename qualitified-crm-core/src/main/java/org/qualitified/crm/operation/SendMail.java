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
import org.nuxeo.ecm.core.api.*;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import org.nuxeo.runtime.api.Framework;
import org.qualitified.crm.service.EmailingService;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

@Operation(id = SendMail.ID, category = Constants.CAT_EXECUTION, label = "SendMail", description = "SendMail...")
public class SendMail {

    public final static String ID = "Qualitified.SendMail";
    private Log logger = LogFactory.getLog(SendMail.class);


    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession documentManager;
    private String MessageID;


    @OperationMethod
    public void run(DocumentModel interactionDoc) throws OperationException, LoginException, JSONException, MailjetSocketTimeoutException, MailjetException {
        EmailingService emailingService = Framework.getService(EmailingService.class);
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);
        IdRef idRef = new IdRef((String) interactionDoc.getPropertyValue("interaction:campaignId"));
        DocumentModel campaignDoc = documentManager.getDocument(idRef);

        List<String> mailDetails = new ArrayList<>();

        String fromEmail = (String) interactionDoc.getPropertyValue("interaction:fromEmail");
        mailDetails.add(fromEmail);
        String fromName = (String) interactionDoc.getPropertyValue("interaction:fromName");
        mailDetails.add(fromName);
        String toEmail = (String) interactionDoc.getPropertyValue("interaction:toEmail");
        mailDetails.add(toEmail);
        String toName = (String) interactionDoc.getPropertyValue("interaction:toName");
        mailDetails.add(toName);
        String subject = (String) campaignDoc.getPropertyValue("campaign:subject");
        mailDetails.add(subject);
        String textPart = (String) campaignDoc.getPropertyValue("campaign:textPart");
        mailDetails.add(textPart);
        String htmlPart = (String) campaignDoc.getPropertyValue("campaign:htmlPart");
        mailDetails.add(htmlPart);

        JSONArray response = emailingService.send(mailDetails);
        MessageID = Long.toString(response.getJSONObject(0).getJSONArray("To")
                .getJSONObject(0).getLong("MessageID"));

        interactionDoc.setPropertyValue("interaction:messageID", MessageID);
        documentManager.saveDocument(interactionDoc);

    }

}
