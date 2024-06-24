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
import java.util.*;
import java.util.Map;
/**
 * this file is used to retrieve all the custom fields that contains the option "show in result list"
 */
@Operation(id=GetResultListCustomFields.ID, category= Constants.CAT_EXECUTION, label="GetResultListCustomFields", description="")
public class GetResultListCustomFields {
    public static final String ID = "Qualitified.GetResultListCustomFields";
    private Log logger = LogFactory.getLog(GetResultListCustomFields.class);
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
            List<Map<String,Object>> customFieldList  = (List<Map<String,Object>>)customDocument.getPropertyValue("custom:field");
            List<Map<String, Object>> customFieldList2 = new ArrayList<>();



                for (Map<String, Object> listItem : customFieldList) {
                   /* if(listItem.containsKey("showInResultList")){
                        if(Boolean.parseBoolean(listItem.get("showInResultList"))){
                            customFieldList2.add(listItem);
                        }
                    }*/
                    listItem.entrySet().forEach((entry) -> {
                                if (entry.getKey().equals("showInResultList") && entry.getValue() != null && (Boolean) entry.getValue()) {

                                   // customFieldList2.add(listItem);
                                    customFieldList2.add(listItem);
                                }
                            }
                    );
                }


            ObjectMapper object = new ObjectMapper();
            JSONArray = object.writeValueAsString(customFieldList2);

        }
        ctx.getLoginStack().pop();
        return new StringBlob(JSONArray, "application/json");
    }
}