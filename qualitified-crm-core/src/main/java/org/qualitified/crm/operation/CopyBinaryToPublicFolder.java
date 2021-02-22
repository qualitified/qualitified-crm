package org.qualitified.crm.operation;

import org.json.JSONException;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.runtime.api.Framework;

import java.io.*;

@Operation(id = CopyBinaryToPublicFolder.ID, category = Constants.CAT_EXECUTION, label = "Copy Binary To Public Folder", description = "...")

public class CopyBinaryToPublicFolder {
    public final static String ID = "Qualitified.CopyBinaryToPublicFolder";
    String folderPath= Framework.getProperty("public.folder.path");
    String serverPath=Framework.getProperty("nuxeo.url");
    @Context
    protected CoreSession session;
    @Context
    protected CoreSession documentManager;
    @OperationMethod()
    public void run() throws IOException, OperationException, JSONException {
        DocumentModel image = null;

        DocumentModelList imageList = session.query("SELECT * FROM Picture WHERE  ecm:path STARTSWITH  '/Marketing/Resources/' AND ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 0 AND ecm:isVersion = 0 AND ecm:isTrashed = 0");

        if (imageList.size() > 0) {
           for (DocumentModel imagedoc:imageList){
               File source = new File(imagedoc.getPathAsString());
               Blob blob = (Blob)imagedoc.getPropertyValue("file:content");
               System.out.println("the source is = " + source);
               File dest = new File(folderPath+blob.getFilename());
               blob.transferTo(dest);
               String[] path= serverPath.split("nuxeo");
               imagedoc.setPropertyValue("custom:stringField1",path[0]+"resources/"+blob.getFilename());
               documentManager.saveDocument(imagedoc);
               long start = System.nanoTime();
               System.out.println("Time taken by Stream Copy = " + (System.nanoTime() - start));
           }
        }
    }




}


