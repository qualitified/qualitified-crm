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
import sun.security.util.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

@Operation(id = LoadFunctionalTest.ID, category = Constants.CAT_EXECUTION, label = "Run Functional Test", description = "Load Functional Test by copying the test in the qualitified-functional-test.html file.")

public class LoadFunctionalTest {
    public final static String ID = "Qualitified.LoadFunctionalTest";

    private Log logger = LogFactory.getLog(LoadFunctionalTest.class);

    @Param(name = "path", required = false)
    protected String path;

    @Param(name = "metadata", required = false)
    protected String metadata;

    @Context
    protected CoreSession session;

    @OperationMethod()
    public void run(DocumentModel document) throws IOException, OperationException, JSONException {
        String folderPath = (path != null && path != "") ? path : Framework.getProperty("nuxeo.war.dir");
        metadata = (metadata != null && metadata != "") ? metadata : "note:note";
        String content = (String)document.getPropertyValue("note:note");
        if (content != null){
            File template = new File(folderPath+"/ui/qualitified-functional-test.html.ftl");
            String fileContent  = new Scanner(template).useDelimiter("\\Z").next();
            fileContent = fileContent.replace("${qualitified.functional.test.scenario}", content);
            File dest = new File(folderPath+"/ui/qualitified-functional-test.html");
            writeToFile(fileContent, dest);
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








