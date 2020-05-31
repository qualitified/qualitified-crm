package org.qualitified.crm.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.runtime.api.Framework;
import java.util.List;

@Operation(id= GetActions.ID, category= Constants.CAT_EXECUTION, label="Get Actions", description="")
public class GetActions {
    public static final String ID = "Qualitified.GetActions";
    private Log logger = LogFactory.getLog(GetActions.class);

    @Context
    protected OperationContext ctx;

    @OperationMethod
    public Blob run() throws Exception {
        String adminPath = Framework.getProperty("qualitified.config.path", "/Admin");
        List<DocumentModel> scriptActions = CoreInstance.doPrivileged(ctx.getCoreSession(), (session) -> {
            return session.query("SELECT * FROM Scripts WHERE ecm:path STARTSWITH '"+adminPath+"' AND ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:isTrashed = 0 AND (scriptnote:isDisabled is NULL OR scriptnote:isDisabled = 0) AND ecm:currentLifeCycleState != 'deleted' AND scripts:scriptType='action' ORDER BY scriptnote:order ASC, dc:created ASC");
        });

        JSONArray jsonArray = new JSONArray();
        for(DocumentModel scriptAction : scriptActions) {
            JSONObject json = getValueAsJSON(scriptAction);
            jsonArray.put(json);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("actions", jsonArray);
        return new StringBlob(jsonObject.toString(), "application/json");
    }

    private JSONObject getValueAsJSON(DocumentModel doc){
        return CoreInstance.doPrivileged(ctx.getCoreSession(), (session) -> {
            DocumentModel script = session.getDocument(new IdRef(doc.getId()));
            JSONObject json = new JSONObject();
            try {
                json.put("title", script.getTitle());
                json.put("uid", script.getId());
                json.put("path", script.getPath());
                json.put("scripts:documentTypes", script.getPropertyValue("scripts:documentTypes"));
                json.put("scripts:groups",  script.getPropertyValue("scripts:groups"));
                json.put("scripts:expression",  script.getPropertyValue("scripts:expression"));
                json.put("scripts:path",  script.getPropertyValue("scripts:path"));
                json.put("scripts:permission",  script.getPropertyValue("scripts:permission"));
                json.put("scripts:state",  script.getPropertyValue("scripts:state"));
                json.put("scripts:facet",  script.getPropertyValue("scripts:facet"));
                json.put("scripts:icon",  script.getPropertyValue("scripts:icon"));
            } catch (JSONException e) {
               logger.error("Error while trying to put action values in JSON");
               throw (new NuxeoException(e));
            }
            return json;
        });
    }

}
