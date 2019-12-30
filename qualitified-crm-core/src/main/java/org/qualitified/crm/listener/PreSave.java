package org.qualitified.crm.listener;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;

import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.runtime.api.Framework;

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

        String adminPath = Framework.getProperty("qualitified.config.path", "/Admin");
        PathRef destinationRef = (PathRef)event.getContext().getProperty("destinationRef");
        if(destinationRef != null && destinationRef.toString().startsWith(adminPath)){
            logger.warn("Document is in config path ["+adminPath+"]. We won't run PreSave.");
            logger.warn("The admin path can be changed in nuxeo.conf [qualitified.config.path].");
            return;
        }

        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();


        List<DocumentModel> scripts = CoreInstance.doPrivileged(docCtx.getCoreSession(), (session) -> {
            return session.query("SELECT * FROM ScriptNote WHERE ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:isTrashed = 0 AND ecm:currentLifeCycleState != 'deleted' AND dc:title ILIKE '"+doc.getType()+"PreSave%' ORDER BY scriptnote:order ASC, dc:created ASC");
        });
        for(DocumentModel script : scripts) {
            OperationContext operationContext = new OperationContext(docCtx.getCoreSession());
            operationContext.setInput(doc);
            Map<String, Object> params = new HashMap<>();
            String scriptNote = (String) script.getPropertyValue("note:note");

            params.put("script", scriptNote);
            params.put("isCreation", ("aboutToCreate").equals(event.getName()));

            AutomationService automationService = Framework.getService(AutomationService.class);

            try {
                Object obj = automationService.run(operationContext, "javascript.preSave", params);
                if(obj instanceof DocumentModel){
                    doc.copyContent((DocumentModel)obj);
                }
            } catch (OperationException e) {
                logger.error("Error while running automation script javascript.preSave", e);
            }
            catch (ClassCastException e) {
                logger.error("Error while running automation script javascript.preSave", e);
            }
        }
    }
}
