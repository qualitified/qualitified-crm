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
public class Copy implements EventListener {

    private Log logger = LogFactory.getLog(Copy.class);

    @Override
    public void handleEvent(Event event) {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }

        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();
        if (doc.getPath().toString().startsWith("/Marketing/Resources/") && doc.getType().equals("Picture")) {
            OperationContext operationContext = new OperationContext(docCtx.getCoreSession());
            operationContext.setInput(doc);
            AutomationService automationService = Framework.getService(AutomationService.class);
            try {
                automationService.run(operationContext, "Qualitified.CopyBinaryToPublicFolder");
            } catch (OperationException e) {
                logger.error("Error while running operation Qualitified.CopyBinaryToPublicFolder", e);
            }
        }
    }
}