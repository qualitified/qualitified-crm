package org.qualitified.crm.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InteractionPostSaveListener implements EventListener {

    private Log logger = LogFactory.getLog(InteractionPostSaveListener.class);

    @Override
    public void handleEvent(Event event) {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }
        /*Boolean block = (Boolean) event.getContext().getProperty("custom:booleanField2");
        if (Boolean.TRUE.equals(block)) {
            return;
        }*/
        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel interactionDoc = docCtx.getSourceDocument();
        UserManager userManager = Framework.getService(UserManager.class);
        AutomationService automationService = Framework.getService(AutomationService.class);
        OperationContext operationContext = new OperationContext(docCtx.getCoreSession());
        operationContext.setInput(interactionDoc);
        if (!("Interaction".equals(interactionDoc.getType())) || interactionDoc.getId() == null) {
            return;
        }

        try {
             boolean isSynced = interactionDoc.getPropertyValue("custom:booleanField1") != null && (boolean) interactionDoc.getPropertyValue("custom:booleanField1");
             boolean toSend = interactionDoc.getPropertyValue("custom:booleanField2") != null && (boolean) interactionDoc.getPropertyValue("custom:booleanField2");

            if ( Boolean.TRUE.equals(isSynced)) {
                 logger.warn("Running SynchronizeWithCalendar on event "+ interactionDoc.getTitle());
                 automationService.run(operationContext, "Qualitified.SynchronizeWithCalendar");
            }
            if ( Boolean.TRUE.equals(toSend)) {
                Map<String, Object> params = new HashMap<>();

                String[] responsibleList = (String[]) interactionDoc.getPropertyValue("interaction:responsible");
                NuxeoPrincipal principal= userManager.getPrincipal(responsibleList[0]);
                String senderEmail = principal.getEmail();

                StringList contactsEmail = new StringList();
                String[] contacts = (String[]) interactionDoc.getPropertyValue("interaction:contact");
                for (String contact : contacts) {
                    DocumentModel contactDoc = docCtx.getCoreSession().getDocument(new IdRef(contact));
                    contactsEmail.add((String) contactDoc.getPropertyValue("person:email"));
                }
                params.put("from", senderEmail);
                params.put("message", interactionDoc.getPropertyValue("note:note"));
                params.put("subject", interactionDoc.getTitle());
                params.put("to", contactsEmail);
                params.put("HTML", true);
                params.put("rollbackOnError", false);

                logger.warn("Running Sending Mail on interaction"+ interactionDoc.getTitle());
                automationService.run(operationContext, "Document.Mail",params);
            }
         } catch (OperationException e) {
             logger.error("Error while running operations...", e);
         }

    }
}