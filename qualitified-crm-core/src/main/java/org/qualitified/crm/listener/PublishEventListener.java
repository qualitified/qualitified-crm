package org.qualitified.crm.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.runtime.api.Framework;

public class PublishEventListener implements EventListener {

    private Log logger = LogFactory.getLog(PublishEventListener.class);

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
        AutomationService automationService = Framework.getService(AutomationService.class);
        OperationContext operationContext = new OperationContext(docCtx.getCoreSession());
        operationContext.setInput(interactionDoc);
        if (!("Interaction".equals(interactionDoc.getType())) || interactionDoc.getId() == null) {
            return;
        }

        try {
             boolean isSynced = (boolean) interactionDoc.getPropertyValue("custom:booleanField1");
             if ( Boolean.TRUE.equals(isSynced)) {
                 logger.warn("Running SynchronizeWithCalendar on event "+ interactionDoc.getTitle());
                 automationService.run(operationContext, "Qualitified.SynchronizeWithCalendar");
             } else {
                 logger.warn("Running SynchronizeWithCalendar... "+interactionDoc.getTitle()+" is not synced with google calendar.");
             }
         } catch (OperationException e) {
             logger.error("Error while running operation Qualitified.SynchronizeWithCalendar", e);
         }

    }
}