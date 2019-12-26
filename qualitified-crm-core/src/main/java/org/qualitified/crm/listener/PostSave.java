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

        String script = CoreInstance.doPrivileged(docCtx.getCoreSession(), (session) -> {
            List<DocumentModel> scripts = session.query("SELECT * FROM ScriptNote WHERE ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:isTrashed = 0 AND ecm:currentLifeCycleState != 'deleted' AND dc:title = '"+doc.getType()+"PostSave'");
            if(scripts.size()>0) {
                String scriptNote = (String) scripts.get(0).getPropertyValue("note:note");
                return scriptNote;
            }else{
                return null;
            }
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

        if(script != null && !("").equals(script)) {
            OperationContext operationContext = new OperationContext(docCtx.getCoreSession());
            operationContext.setInput(doc);
            Map<String, Object> params = new HashMap<>();

            params.put("script", script);
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
