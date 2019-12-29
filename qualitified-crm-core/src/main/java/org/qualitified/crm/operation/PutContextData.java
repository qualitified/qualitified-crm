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

/**
 * Created by mgena on 11/11/2017.
 */
@Operation(id= PutContextData.ID, category= Constants.CAT_EXECUTION, label="PutContextData", description="")
public class PutContextData {
    public static final String ID = "Qualitified.PutContextData";
    private Log logger = LogFactory.getLog(PutContextData.class);

    @Context
    protected OperationContext ctx;

    @Param(name = "variable")
    protected String variable;

    @Param(name = "value")
    protected String value;

    @OperationMethod
    public DocumentModel run(DocumentModel doc) throws Exception {
        LoginContext lc = Framework.loginAsUser("Administrator");
        doc.putContextData(variable, value);
        ctx.getLoginStack().pop();
        return doc;

    }

}