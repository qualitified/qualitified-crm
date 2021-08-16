package org.qualitified.crm.operation;

import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;

import com.mailjet.client.MailjetResponse;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.event.EventServiceAdmin;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;
import org.qualitified.crm.listener.InteractionPostSaveListener;
import org.qualitified.crm.service.EmailingService;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.Map;


@Operation(id = SendEmailFromInteraction.ID, category = Constants.CAT_EXECUTION, label = "SendEmailFromInteraction", description = "SendEmailFromInteraction...")
public class SendEmailFromInteraction {
    public final static String ID = "Qualitified.SendEmailFromInteraction";
    private final Log logger = LogFactory.getLog(SendEmailFromInteraction.class);

    @Context
    protected OperationContext ctx;
    @Context
    protected CoreSession documentManager;

    @OperationMethod
    public void run(DocumentModel interactionDoc) throws OperationException, LoginException, JSONException, MailjetSocketTimeoutException {

        EventServiceAdmin eventServiceAdmin = Framework.getService(EventServiceAdmin.class);
        UserManager userManager = Framework.getService(UserManager.class);
        EmailingService emailingService = Framework.getService(EmailingService.class);
        Map<String, Object> params = new HashMap<>();

        try {

            String[] responsibleList = (String[]) interactionDoc.getPropertyValue("interaction:responsible");
            NuxeoPrincipal principal = userManager.getPrincipal(responsibleList[0]);
            String senderEmail = principal.getEmail();
            String senderName = principal.getFirstName() + ' ' + principal.getLastName();

            StringList contactsEmail = new StringList();
            String[] contacts = (String[]) interactionDoc.getPropertyValue("interaction:contact");
            Map<String, Object> mailDetails = new HashMap<String, Object>();

            mailDetails.put("fromEmail", senderEmail);
            mailDetails.put("fromName", senderName);
            mailDetails.put("subject", interactionDoc.getTitle());
            mailDetails.put("textPart", interactionDoc.getPropertyValue("note:note"));
            mailDetails.put("htmlPart", "");
            for (String contact : contacts) {
                DocumentModel contactDoc = documentManager.getDocument(new IdRef(contact));
                contactsEmail.add((String) contactDoc.getPropertyValue("person:email"));
                String contactEmail = (String) contactDoc.getPropertyValue("person:email");
                String contactName = (String) contactDoc.getPropertyValue("person:firstName") + ' ' + contactDoc.getPropertyValue("person:lastName");
                mailDetails.put("toEmail", contactEmail);
                mailDetails.put("toName", contactName);
                MailjetResponse response = emailingService.send(mailDetails);
                logger.warn(response.getData());
                if (response.getJSONArray("Messages").getJSONObject(0).getString("Status").equals("success")) {
                    interactionDoc.setPropertyValue("interaction:isSent", 1);
                } else {
                    interactionDoc.setPropertyValue("interaction:isSent", 0);
                }
                eventServiceAdmin.setListenerEnabledFlag("interactionPostSaveListener", false);
                documentManager.saveDocument(interactionDoc);
            }

        } catch (MailjetSocketTimeoutException | JSONException | MailjetException e) {
            e.printStackTrace();
        } finally {
            eventServiceAdmin.setListenerEnabledFlag("interactionPostSaveListener",true);
        }

    }
}