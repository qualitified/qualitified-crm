package org.qualitified.crm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.runtime.api.Framework;
import javax.security.auth.login.LoginContext;

/**
 * Created by mgena on 11/11/2017.
 */
@Operation(id=GetCustomFields.ID, category= Constants.CAT_EXECUTION, label="GetCustomFields", description="")
public class GetCustomFields {
    public static final String ID = "Qualitified.GetCustomFields";
    private Log logger = LogFactory.getLog(GetCustomFields.class);

    @Context
    protected OperationContext ctx;

    @Param(name = "documentType")
    protected String documentType;

    @OperationMethod
    public DocumentModelList run() throws Exception {
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);
        DocumentModelList customFields = ctx.getCoreSession().query("SELECT * FROM Custom WHERE ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted' AND dc:title='"+documentType+"'", "NXQL", null, 0, 0, false);
        ctx.getLoginStack().pop();
        return customFields;
    }

}
