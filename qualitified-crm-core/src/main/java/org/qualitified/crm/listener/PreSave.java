package org.qualitified.crm.listener;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;

import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.runtime.api.Framework;
import org.qualitified.crm.GetCustomFields;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreSave implements EventListener {

    private Log logger = LogFactory.getLog(PreSave.class);

    @Override
    public void handleEvent(Event event) {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
          return;
        }

        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();


        String script = CoreInstance.doPrivileged(docCtx.getCoreSession(), (session) -> {
            List<DocumentModel> scripts = session.query("SELECT * FROM ScriptNote WHERE ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted' AND dc:title = '"+doc.getType()+"PreSave'");
            if(scripts.size()>0) {
                String scriptNote = (String) scripts.get(0).getPropertyValue("note:note");
                return scriptNote;
            }else{
                return null;
            }

        });
        if(script != null && !("").equals(script)) {
            OperationContext operationContext = new OperationContext(docCtx.getCoreSession());
            operationContext.setInput(doc);
            Map<String, Object> params = new HashMap<>();

            params.put("script", script);
            params.put("isCreation", ("aboutToCreate").equals(event.getName()));

            AutomationService automationService = Framework.getService(AutomationService.class);
            DocumentModel updatedDoc = null;
            try {
                updatedDoc = (DocumentModel) automationService.run(operationContext, "javascript.preSave", params);
                doc.copyContent(updatedDoc);
            } catch (OperationException e) {
                logger.error("Error while running automation script javascript.preSave", e);
            } catch (ClassCastException e){
                logger.error("Error while running automation script javascript.preSave", e);
            }
        }
    }
}
