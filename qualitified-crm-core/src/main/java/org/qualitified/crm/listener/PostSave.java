package org.qualitified.crm.listener;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.runtime.api.Framework;

import java.util.*;

public class PostSave implements EventListener {

    private Log logger = LogFactory.getLog(PostSave.class);

    @Override
    public void handleEvent(Event event) {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
          return;
        }

        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();

        List<DocumentModel> scripts = CoreInstance.doPrivileged(docCtx.getCoreSession(), (session) -> {
            return session.query("SELECT * FROM ScriptNote WHERE ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:isTrashed = 0 AND ecm:currentLifeCycleState != 'deleted' AND dc:title ILIKE '"+doc.getType()+"PostSave%' ORDER BY dc:created ASC");
        });

        CoreInstance.doPrivileged(docCtx.getCoreSession(), (session) -> {
            DocumentModelList docList = session.getChildren(new IdRef(doc.getId()));
            if(("documentCreated").equals(event.getName()) && !doc.getPathAsString().startsWith("/Admin/Templates/") && docList.totalSize() == 0){
                String templatePath = "/Admin/Templates/"+ doc.getType();
                DocumentModelList templateDoc = session.query("SELECT * FROM Document WHERE ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:isTrashed = 0 AND ecm:currentLifeCycleState != 'deleted' AND ecm:primaryType = '"+doc.getType()+"' AND ecm:path = '"+templatePath+"'");
                if(templateDoc.totalSize()>0){
                    DocumentModelList docs = session.getChildren(new IdRef(templateDoc.get(0).getId()));
                    docs.forEach(document -> {
                        session.copy(Collections.singletonList(document.getRef()), new IdRef(doc.getId()));
                    });
                }
            }
        });

        for(DocumentModel script : scripts) {
            OperationContext operationContext = new OperationContext(docCtx.getCoreSession());
            operationContext.setInput(doc);
            Map<String, Object> params = new HashMap<>();
            String scriptNote = (String) script.getPropertyValue("note:note");

            params.put("script", scriptNote);
            params.put("isCreation", ("documentCreated").equals(event.getName()));

            AutomationService automationService = Framework.getService(AutomationService.class);

            try {
                automationService.run(operationContext, "javascript.postSave", params);
            } catch (OperationException e) {
                logger.error("Error while running automation script javascript.postSave", e);
            }
            catch (ClassCastException e) {
                logger.error("Error while running automation script javascript.postSave", e);
            }
        }
    }
}