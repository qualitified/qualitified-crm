package org.qualitified.crm.operation;

import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Operation(id = CreateInteractions.ID, category = Constants.CAT_EXECUTION, label = "CreateInteractions", description = "SendMail...")
public class CreateInteractions {
    public final static String ID = "Qualitified.CreateInteractions";
    @Context
    protected OperationContext ctx;
    @Context
    protected CoreSession documentManager;
    @Param(name = "campaignId")
    protected String campaignId;

    private Log logger = LogFactory.getLog(AutomationSendMail.class);
    private ArrayList<String> contacts = new ArrayList<>();


    @OperationMethod
    public void run(DocumentModel contactDoc) throws OperationException, LoginException, JSONException, MailjetSocketTimeoutException {
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);

        DocumentModel interactionDoc = documentManager.createDocumentModel("/Marketing", "interaction", "Interaction");
        interactionDoc.setPropertyValue("dc:title", "Mail to "+ contactDoc.getTitle());
        interactionDoc.setPropertyValue("interaction:status", "TODO");
        interactionDoc.setPropertyValue("interaction:activity", "Emailing");
        interactionDoc.setPropertyValue("interaction:date", Calendar.getInstance());
        interactionDoc.setPropertyValue("interaction:campaignId",campaignId);
        contacts.add(contactDoc.getId());
        interactionDoc.setPropertyValue("interaction:contact",contacts);
        documentManager.createDocument(interactionDoc);



    }
}
