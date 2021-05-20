package org.qualitified.crm.operation;

import org.json.JSONException;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

import java.io.IOException;

@Operation(id = RemoveSpaceFromTitle.ID, category = Constants.CAT_EXECUTION, label = "remove space from title", description = "...")

public class RemoveSpaceFromTitle {
    public final static String ID = "Qualitified.RemoveSpaceFromTitle";
    @Context
    protected CoreSession documentManager;
    @OperationMethod()
    public void run(DocumentModel doc) throws IOException, OperationException, JSONException {
        String title= (String) doc.getPropertyValue("dc:title");
       // String  path= String.valueOf(doc.getPath());
        //String[] path1= path.split("/");
        String result = title.trim();
        doc.setPropertyValue("dc:title",result);
       //String newPath=doc.getPath().toString().split("/")[1]+"/"+result;
        doc.setPathInfo(documentManager.getParentDocument(doc.getRef()).getPath().toString(),doc.getTitle());
    }

}
