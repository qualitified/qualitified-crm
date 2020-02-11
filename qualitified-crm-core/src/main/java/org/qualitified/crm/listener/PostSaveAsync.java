package org.qualitified.crm.listener;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.event.*;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.runtime.api.Framework;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostSaveAsync implements PostCommitFilteringEventListener {

    private Log logger = LogFactory.getLog(PostSaveAsync.class);

    protected final List<String> handled = Arrays.asList("documentCreated", "documentModified");


    @Override
    public void handleEvent(EventBundle events) {
        for (Event event : events) {
            if (acceptEvent(event)) {
                handleEvent(event);
            }
        }
    }

    @Override
    public boolean acceptEvent(Event event) {
        return handled.contains(event.getName());
    }

    protected void handleEvent(Event event) {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }

        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();

        String adminPath = Framework.getProperty("qualitified.config.path", "/Admin");
        PathRef destinationRef = (PathRef) event.getContext().getProperty("destinationRef");
        if (destinationRef != null && destinationRef.toString().startsWith(adminPath)) {
            logger.warn("Document is in config path [" + adminPath + "]. We won't run PostSaveAsync.");
            logger.warn("The admin path can be changed in nuxeo.conf [qualitified.config.path].");
            return;
        }

        List<DocumentModel> scripts = CoreInstance.doPrivileged(docCtx.getCoreSession(), (session) -> {
            return session.query("SELECT * FROM ScriptNote WHERE ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:isTrashed = 0 AND (scriptnote:isDisabled is NULL OR scriptnote:isDisabled = 0) AND ecm:currentLifeCycleState != 'deleted' AND dc:title ILIKE '" + doc.getType() + "PostSaveAsync%' AND scriptnote:async = 1 ORDER BY scriptnote:order ASC, dc:created ASC");
        });

        for (DocumentModel script : scripts) {
            OperationContext operationContext = new OperationContext(docCtx.getCoreSession());
            operationContext.setInput(doc);
            Map<String, Object> params = new HashMap<>();
            String scriptNote = CoreInstance.doPrivileged(docCtx.getCoreSession(), (session) -> {
                return (String) session.getDocument(new IdRef(script.getId())).getPropertyValue("note:note");
            });

            params.put("script", scriptNote);
            params.put("isCreation", ("documentCreated").equals(event.getName()));

            AutomationService automationService = Framework.getService(AutomationService.class);

            try {
                automationService.run(operationContext, "javascript.postSaveAsync", params);
                DocumentModel docUpdated = doc.getCoreSession().getDocument(new IdRef(doc.getId()));
                doc.copyContent(docUpdated);
            } catch (OperationException e) {
                logger.error("Error while running automation script javascript.postSaveAsync", e);
            } catch (ClassCastException e) {
                logger.error("Error while running automation script javascript.postSaveAsync", e);
            }
        }
    }

}
