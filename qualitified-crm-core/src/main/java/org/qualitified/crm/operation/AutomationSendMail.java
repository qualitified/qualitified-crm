package org.qualitified.crm.operation;

import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mchange.util.StringObjectMap;
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
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.runtime.api.Framework;
import org.qualitified.crm.service.EmailingService;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.Serializable;
import java.util.*;

@Operation(id = AutomationSendMail.ID, category = Constants.CAT_EXECUTION, label = "AutomationSendMail", description = "SendMail...")
public class AutomationSendMail {

    public final static String ID = "Qualitified.AutomationSendMail";
    @Context
    protected OperationContext ctx;
    @Context
    protected CoreSession documentManager;
    @Param(name = "subject")
    protected String mailSubject;
    @Param(name = "htmlPart")
    protected String mailHtmlPart;

    private Log logger = LogFactory.getLog(AutomationSendMail.class);
    private String fromEmail=Framework.getProperty("mailjetService.fromEmail");
    private String fromName=Framework.getProperty("mailjetService.fromName");

    private String MessageID;
    private Map<String, Object> mailDetails = new HashMap<String, Object>();


    @OperationMethod
    public void run(DocumentModel interactionDoc) throws OperationException, LoginException, JSONException, MailjetSocketTimeoutException {
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);

        EmailingService emailingService = Framework.getService(EmailingService.class);

        String[] contacts= (String[]) interactionDoc.getPropertyValue("interaction:contact");
        DocumentModel contactDoc = documentManager.getDocument(new IdRef(contacts[0]));

        mailDetails.put("fromEmail",fromEmail);
        mailDetails.put("fromName",fromName);
        String toEmail = (String) contactDoc.getPropertyValue("person:email");
        mailDetails.put("toEmail",toEmail);
        String firstName = (contactDoc.getPropertyValue("person:firstName")!= null)
                         ? (String) contactDoc.getPropertyValue("person:firstName")
                         : "";
        String lastName = (contactDoc.getPropertyValue("person:lastName")!= null)
                        ? (String) contactDoc.getPropertyValue("person:lastName")
                        : "";
        String toName = lastName +""+ firstName;
        mailDetails.put("toName",toName);
        String subject = mailSubject;
        mailDetails.put("subject",subject);
        String textPart ="";
        mailDetails.put("textPart",textPart);
        String htmlPart = mailHtmlPart;
        mailDetails.put("htmlPart",htmlPart);

        try {
            JSONArray response = emailingService.send(mailDetails);
            MessageID = Long.toString(response.getJSONObject(0).getJSONArray("To")
                    .getJSONObject(0).getLong("MessageID"));
            interactionDoc.setPropertyValue("interaction:messageID",MessageID);
        } catch (MailjetException m) {
            logger.error("Error while running mailjet service", m);
        } catch (JSONException j) {
            logger.error("Mailjet service authentication error,expired or unvalidated apikey", j);
        } finally {
            interactionDoc.setPropertyValue("interaction:status", "DONE");
            documentManager.saveDocument(interactionDoc);
            logger.info("Message with subject " +mailSubject+ "sent to " +toEmail+ "from" +fromEmail);
        }


    }

}
