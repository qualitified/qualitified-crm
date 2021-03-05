package org.qualitified.crm.operation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.picture.api.PictureView;
import org.nuxeo.ecm.platform.picture.api.adapters.MultiviewPicture;
import org.nuxeo.runtime.api.Framework;
import java.io.*;

@Operation(id = CopyBinaryToPublicFolder.ID, category = Constants.CAT_EXECUTION, label = "Copy Binary To Public Folder", description = "...")

public class CopyBinaryToPublicFolder {
    public final static String ID = "Qualitified.CopyBinaryToPublicFolder";
    String folderPath= Framework.getProperty("public.folder.path");
    String serverPath=Framework.getProperty("nuxeo.url");
    private Log logger = LogFactory.getLog(CopyBinaryToPublicFolder.class);

    @Context
    protected CoreSession session;
    @Context
    protected CoreSession documentManager;
    @OperationMethod()
    public void run(DocumentModel image) throws IOException, OperationException, JSONException {


          // File source = new File(image.getPathAsString());
        MultiviewPicture multiviewPicture = image.getAdapter(MultiviewPicture.class);

        PictureView picture= multiviewPicture.getView("Medium");
           Blob blob=picture.getBlob();
           logger.warn("source is" +blob);

           if (blob!=null){
                File dest = new File(folderPath+image.getPropertyValue("dc:title"));
                blob.transferTo(dest);
                String[] path= serverPath.split("nuxeo");
                image.setPropertyValue("public:url",path[0]+"resources/"+image.getPropertyValue("dc:title"));
           }
    }
}








