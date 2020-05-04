package org.qualitified.crm.operation;

import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

@Operation(id= Log.ID, category= Constants.CAT_EXECUTION, label="Install", description="Get Log from a remote nuxeo server.")
public class Log {
    public static final String ID = "Qualitified.Log";
    private org.apache.commons.logging.Log logger = LogFactory.getLog(Log.class);

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "privateKey", required = true)
    protected String privateKey;

    @Param(name = "host", required = true)
    protected String host;

    @Param(name = "login", required = false)
    protected String login;

    @OperationMethod
    public Blob run() throws Exception {

        if(login == null || ("").equals(login)){
            login = "ubuntu";
        }
        Process p = Runtime.getRuntime().exec("ssh -i "+privateKey+" "+login+"@"+host+" './log.sh'  -o StrictHostKeyChecking=no");

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

        if(!("").equals(errorWriter.toString())){
            throw new NuxeoException("Error while trying to get log from a remote nuxeo server: "+ errorWriter.toString());
        }
        return new StringBlob("{\"status\":200,\"result\":\""+stringWriter.toString()+"\"}","application/json");
    }

}