package org.qualitified.crm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.runtime.api.Framework;

import javax.inject.Inject;
import javax.security.auth.login.LoginContext;

/**
 * Created by mgena on 11/11/2017.
 */
@Operation(id= Save.ID, category= Constants.CAT_EXECUTION, label="Save", description="")
public class Save {
    public static final String ID = "Qualitified.Save";
    private Log logger = LogFactory.getLog(Save.class);

    @Context
    protected OperationContext ctx;

    /*@Param(name = "login")
    protected String login;*/

    @OperationMethod
    public DocumentModel run(DocumentModel doc) throws Exception {
        LoginContext lc = Framework.loginAsUser(ctx.getPrincipal().getName());
        ctx.getLoginStack().push(lc);
        CoreSession session = ctx.getCoreSession();
        doc = session.saveDocument(doc);
        //session.save();
        ctx.getLoginStack().pop();
        return doc;
    }

}