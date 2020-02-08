package org.qualitified.crm.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.api.Framework;
import javax.security.auth.login.LoginContext;

@Operation(id= RecordAction.ID, category= Constants.CAT_EXECUTION, label="RecordAction", description="Records the action by creating a script")
public class RecordAction {
    public static final String ID = "Qualitified.RecordAction";
    private Log logger = LogFactory.getLog(RecordAction.class);

    @Context
    protected OperationContext ctx;

    @Param(name = "actionName")
    protected String actionName;

    @Param(name = "actionContent")
    protected String actionContent;

    @OperationMethod
    public DocumentModel run(DocumentModel doc) throws Exception {
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);
        DocumentModel action = ctx.getCoreSession().createDocumentModel(doc.getPathAsString(), this.actionName, "ScriptNote");
        action.setPropertyValue("note:mime_type", "text/plain");
        action.setPropertyValue("note:note", this.actionContent);
        ctx.getCoreSession().createDocument(action);
        ctx.getCoreSession().save();
        ctx.getLoginStack().pop();
        return action;
    }
}
