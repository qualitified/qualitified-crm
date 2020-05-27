package org.qualitified.crm.operation;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.runtime.api.Framework;
import javax.security.auth.login.LoginContext;
import java.util.List;
import java.util.Map;

/**
 * Created by mgena on 11/11/2017.
 * Modified by mmakni on 19/11/2018.
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
    public Blob run() throws Exception {
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);
        String adminPath = Framework.getProperty("qualitified.config.path", "/Admin");
        DocumentModelList customDocuments = ctx.getCoreSession().query("SELECT * FROM Custom WHERE ecm:path STARTSWITH '"+adminPath+"' AND ecm:isProxy = 0 AND ecm:isTrashed = 0 AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted' AND dc:title='"+documentType+"'", "NXQL", null, 0, 0, false);

        String JSONArray= "";

        if(customDocuments.size()>0){
            DocumentModel customDocument = customDocuments.get(0);
            List<Map<String,String>> customFieldList  = (List<Map<String,String>>)customDocument.getPropertyValue("custom:field");

            //logger.warn("Custom Field List:");
            //logger.warn(customFieldList);

            ObjectMapper object = new ObjectMapper();
            JSONArray = object.writeValueAsString(customFieldList);

            //logger.warn(" JSONArray:");
            //logger.warn(JSONArray);

        }
        ctx.getLoginStack().pop();
        return new StringBlob(JSONArray, "application/json");

    }

}
