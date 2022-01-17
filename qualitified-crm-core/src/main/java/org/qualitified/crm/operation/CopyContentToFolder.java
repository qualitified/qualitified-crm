package org.qualitified.crm.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.api.Framework;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Operation(id = CopyContentToFolder.ID, category = Constants.CAT_EXECUTION, label = "Copy Content To Folder", description = "Copy a content to a folder. File name will be the title of the document. Path is the subfolder where the content will be created.")

public class CopyContentToFolder {
    public final static String ID = "Qualitified.CopyContentToFolder";

    private Log logger = LogFactory.getLog(CopyContentToFolder.class);

    @Param(name = "path", required = false)
    protected String path;

    @Param(name = "metadata", required = false)
    protected String metadata;

    @Context
    protected CoreSession session;

    @OperationMethod()
    public void run(DocumentModel document) throws IOException, OperationException, JSONException {
        String folderPath = (path != null && path != "") ? path : Framework.getProperty("nuxeo.war.dir");
        String configPath=Framework.getProperty("qualitified.config.path", "/Admin");
        metadata = (metadata != null && metadata != "") ? metadata : "note:note";
        String content = (String)document.getPropertyValue("note:note");
        if (content != null){
            File dest = new File(folderPath+document.getPathAsString().replaceFirst(configPath,""));
            writeToFile(content, dest);
        }
    }

    private void writeToFile(String content, File dest) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(dest));
            writer.write(content);
        } catch (IOException e) {
            logger.error("Error while trying to write file", e);
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                logger.error("Error while trying to close file", e);
            }
        }
    }
}








