package org.qualitified.crm.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.runtime.api.Framework;

import javax.security.auth.login.LoginContext;

/**
 * Created by mgena on 11/11/2017.
 */
@Operation(id= GetContextData.ID, category= Constants.CAT_EXECUTION, label="GetContextData", description="")
public class GetContextData {
    public static final String ID = "Qualitified.GetContextData";
    private Log logger = LogFactory.getLog(GetContextData.class);

    @Context
    protected OperationContext ctx;

    @Param(name = "variable")
    protected String variable;

    @OperationMethod
    public Blob run(DocumentModel doc) throws Exception {
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);
        /*JSONObject object = new JSONObject();*/

        String result = doc.getContextData(variable) != null ? (String) doc.getContextData(variable) : "";

        /*if (result != null){
            object.accumulate(variable,result);
        }else{
            object.accumulate(variable,"");
        }*/
        ctx.getLoginStack().pop();
        return new StringBlob(result, "plain/text");
    }

}