package org.qualitified.crm.operation;

import com.sun.jersey.core.util.Base64;
import fr.opensagres.xdocreport.core.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.features.HTTPHelper;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.runtime.api.Framework;
import javax.security.auth.login.LoginContext;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Operation(id= Deploy.ID, category= Constants.CAT_EXECUTION, label="Deploy", description="Deploy the configuration to the specified instance.")
public class Deploy {
    public static final String ID = "Qualitified.Deploy";
    private Log logger = LogFactory.getLog(Deploy.class);

    @Context
    protected OperationContext ctx;

    @Param(name = "url")
    protected String url;

    @Param(name = "login")
    protected String login;

    @Param(name = "password")
    protected String password;

    @OperationMethod
    public Blob run(DocumentModel doc) throws Exception {
        AutomationService automationService = Framework.getService(AutomationService.class);
        LoginContext lc = Framework.loginAsUser("Administrator");
        ctx.getLoginStack().push(lc);

        //Verify that the Unit Tests are passing
        Map<String, Object> params = new HashMap();
        automationService.run(ctx, "Qualitified.RunUnitTests", params);
        DocumentModelList unitTestsFailing = ctx.getCoreSession().query("SELECT * FROM ScriptNote WHERE dc:title ILIKE 'UnitTest%' AND ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:isTrashed = 0 AND ecm:currentLifeCycleState != 'deleted' AND scriptnote:isValid != 1 AND scriptnote:isDisabled != 1");
        if(unitTestsFailing.size()>0){
            logger.warn("Unable to deploy due to some Unit Test(s) error, please fix this before going further.");
            throw new NuxeoException("Unable to deploy due to some Unit Test(s) error, please fix this before going further.",403);
        }

        Map<String, Object> exportParams = new HashMap();
        exportParams.put("exportAsTree", true);
        ctx.setInput(doc);
        Blob export = (Blob)automationService.run(ctx, "Document.Export", exportParams);

        export.setFilename("export.zip");
        export.setMimeType("application/zip");
        File file = File.createTempFile("export", ".zip");
        InputStream in = export.getStream();
        OutputStream out = new FileOutputStream(file);
        IOUtils.copy(in, out);

        //Upload zipExport and get batchId
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost getBatchId = new HttpPost(url+"/api/v1/upload/");
        getBatchId.setHeader("Authorization", "Basic " + new String(Base64.encode(login+":"+password), "ASCII"));
        CloseableHttpResponse response = httpClient.execute(getBatchId);
        HttpEntity responseEntity = response.getEntity();
        String batchId = "";
        if (responseEntity != null) {
            String retSrc = EntityUtils.toString(responseEntity);
            JSONObject batchIdResult = new JSONObject(retSrc);
            batchId = (String)batchIdResult.get(("batchId"));
        }

        HttpPost uploadFile = new HttpPost(url+"/api/v1/upload/"+batchId+"/0");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        // This attaches the file to the POST
        builder.addBinaryBody(
                "file",
                new FileInputStream(file),
                ContentType.APPLICATION_OCTET_STREAM,
                "export.zip"
        );

        HttpEntity multipart = builder.build();
        uploadFile.setEntity(multipart);
        uploadFile.setHeader("Authorization", "Basic " + new String(Base64.encode(login+":"+password), "ASCII"));
        uploadFile.setHeader("X-File-Name", "export.zip");
        uploadFile.setHeader("X-File-Type", "application/zip");
        CloseableHttpResponse uploadResponse = httpClient.execute(uploadFile);

        //Download as ZipTreeExport and deploy on the remote instance
        HTTPHelper httpHelper = new HTTPHelper();
        Blob result = httpHelper.call(login, password, "GET", url+"/api/v1/upload/"+batchId);
        StringWriter writer = new StringWriter();
        IOUtils.copy(result.getStream(), writer, "UTF-8");
        String uploadStatus = writer.toString();
        //TODO Check status of import

        //Import zip as documents
        JSONObject context = new JSONObject();
        DocumentModel parentDoc = ctx.getCoreSession().getDocument(doc.getParentRef());
        context.put("currentDocument", parentDoc.getPathAsString());
        JSONObject param = new JSONObject();
        params.put("context",context);
        JSONObject json = new JSONObject();
        json.put("params", params);
        json.put("context", context);

        HttpPost importZip = new HttpPost(url+"/api/v1/upload/"+batchId+"/execute/FileManager.Import");
        StringEntity jsonEntity = new StringEntity(json.toString());
        jsonEntity.setContentType(String.valueOf(ContentType.APPLICATION_JSON));
        importZip.setEntity(jsonEntity);
        importZip.setHeader("Authorization", "Basic " + new String(Base64.encode(login+":"+password), "ASCII"));
        importZip.setHeader("", "application/json");

        CloseableHttpResponse importZipResponse = httpClient.execute(importZip);
        HttpEntity inportZipResponseEntity = importZipResponse.getEntity();

        String importResult = "";
        if (inportZipResponseEntity != null) {
            importResult = EntityUtils.toString(inportZipResponseEntity);
        }

        ctx.getLoginStack().pop();
        return new StringBlob(importResult, "application/json");
    }
}
