package org.qualitified.crm.operation;

import fr.opensagres.xdocreport.core.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.runtime.api.Framework;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Operation(id= RSync.ID, category= Constants.CAT_EXECUTION, label="RSync", description="Copy Blob to Remote server via rsync")
public class RSync {
    public static final String ID = "Qualitified.RSync";
    private Log logger = LogFactory.getLog(RSync.class);

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "privateKey")
    protected String privateKey;

    @Param(name = "host")
    protected String host;

    @Param(name = "login")
    protected String login;


    @Param(name = "destination")
    protected String destination;

    @OperationMethod
    public Blob run(Blob blob) throws Exception {

        if(destination == null || ("").equals(destination)){
            destination = "/opt/import";
        }

        if(login == null || ("").equals(login)){
            login = "ubuntu";
        }
        File tmpDir = createTempDirectory(new Date().getTime()+"");
        File packageZip = new File(tmpDir, blob.getFilename());
        InputStream input = blob.getStream();
        OutputStream output = new FileOutputStream(packageZip);
        IOUtils.copy(input, output);

        Process p = Runtime.getRuntime().exec(new String[]{"rsync","-avL", "--progress", "-e", "ssh -i  "+privateKey+" -o StrictHostKeyChecking=no", packageZip.getPath(), login+"@"+host+":"+destination});

        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        InputStream errorStream = p.getErrorStream();

        int c = 0;

        StringWriter errorWriter = new StringWriter();
        while ((c = errorStream.read()) != -1) {
            System.out.print((char)c);
            errorWriter.append((char)c);
            logger.error((char)c);
        }

        StringWriter stringWriter = new StringWriter();
        while(in.ready()) {
            String s = in.readLine();
            stringWriter.append(s);
            logger.info(s);
        }

        p.waitFor();
        //delete temp file and folder
        packageZip.delete();
        tmpDir.delete();
        if(!("").equals(errorWriter.toString())){
            throw new NuxeoException("Error while trying to run rsync: "+ errorWriter.toString());
        }
        return new StringBlob("{\"status\":200,\"result\":\""+stringWriter.toString()+"\"}","application/json");
    }

    public static File createTempDirectory(String dirName) {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        File tempDir = new File(baseDir, dirName);
        if (tempDir.mkdir()) {
            return tempDir;
        }
        return null;
    }

}