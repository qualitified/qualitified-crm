package org.qualitified.crm.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.runtime.api.Framework;

import javax.security.auth.login.LoginContext;
import java.util.*;

@Operation(id= RunAction.ID, category= Constants.CAT_EXECUTION, label="RunAction", description="Run an action created by the user.")
public class RunAction {
    public static final String ID = "Qualitified.RunAction";
    private Log logger = LogFactory.getLog(RunAction.class);

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "actionId")
    protected String actionId;

    @Param(name = "actionName")
    protected String actionName;

    @OperationMethod
    public DocumentModel run(DocumentModel doc) throws Exception {

        List<DocumentModel> actions = CoreInstance.doPrivileged(session, (session) -> {
            return session.query("SELECT * FROM ScriptNote WHERE ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:isTrashed = 0 AND (scriptnote:isDisabled is NULL OR scriptnote:isDisabled = 0) AND ecm:currentLifeCycleState != 'deleted' AND ecm:parentId = '"+this.actionId+"' ORDER BY scriptnote:order ASC, dc:created ASC");
        });

        for(DocumentModel action : actions) {
            OperationContext operationContext = new OperationContext(session);
            operationContext.setInput(doc);
            Map<String, Object> params = new HashMap<>();
            String scriptNote = CoreInstance.doPrivileged(session, (session) -> {
                return (String) session.getDocument(new IdRef(action.getId())).getPropertyValue("note:note");
            });

            params.put("script", scriptNote);
            logger.warn("Running Action ["+this.actionName+">"+ action.getTitle() + "]...");
            AutomationService automationService = Framework.getService(AutomationService.class);
            operationContext.put("scriptId", action.getId());
            try {
                Object obj = automationService.run(operationContext, "javascript.runAction", params);
                if (obj instanceof DocumentModel) {
                    doc.copyContent((DocumentModel) obj);
                }
            } catch (OperationException e) {
                logger.error("Error while running automation script javascript.preSave", e);
            } catch (ClassCastException e) {
                logger.error("Error while running automation script javascript.preSave", e);
            }
        }
        return doc;
    }

}