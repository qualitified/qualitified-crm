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
import org.nuxeo.runtime.api.Framework;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Operation(id = CopyBlob.ID, category = Constants.CAT_EXECUTION, label = "Copy blob", description = "...")

public class CopyBlob {


    public final static String ID = "Qualitified.CopyBlob";
    String folderPath = Framework.getProperty("nuxeo.war.dir");
    private Log logger = LogFactory.getLog(org.qualitified.crm.operation.CopyBlob.class);

    @Context
    protected CoreSession session;

    @OperationMethod()
    public void run(DocumentModel image) throws IOException, OperationException, JSONException {

        if(folderPath == null){
            logger.error("nuxeo.war.dir is not defined. Unable to copy Branding images to nuxeo.war.");
            return;
        }

        Blob backgroundImage = (Blob) image.getPropertyValue("branding:backgroundImage");
        copyImage(backgroundImage, folderPath + "/img/background.png");

        Blob loginLogo = (Blob) image.getPropertyValue("branding:loginLogo");
        copyImage(loginLogo, folderPath + "/img/logo.png");

        Blob dashboardLogo = (Blob) image.getPropertyValue("branding:dashboardLogo");
        copyImage(dashboardLogo, folderPath + "/ui/themes/default/logo.png");

        Blob faviconPng = (Blob) image.getPropertyValue("branding:faviconPng");
        copyImage(faviconPng, folderPath + "/icons/favicon.png");

        Blob faviconIco = (Blob) image.getPropertyValue("branding:faviconIco");
        copyImage(faviconIco, folderPath + "/icons/favicon.ico");

        String loginFilePath=folderPath+"/login.jsp";
        String filePath = folderPath+"/ui/themes/default/theme.html";
        List<Map<String, String>> complexValuesList = new ArrayList<Map<String, String>>();
        List<Map<String,String>> styles  = (List<Map<String,String>>)image.getPropertyValue("branding:styles");
        Path path1=Paths.get(loginFilePath);
        Path path = Paths.get(filePath);
        Charset charset = StandardCharsets.UTF_8;
        String content = null;
        String loginContent=null;
        loginContent = new String(Files.readAllBytes(path1), charset);
        loginContent = loginContent.replaceFirst(".*loginButtonBackgroundColor.*","String loginButtonBackgroundColor = LoginScreenHelper.getValueWithDefault(screenConfig.getLoginButtonBackgroundColor(),\" "+ image.getPropertyValue("branding:loginBackgroundColor") +"\");");
        Files.write(path1, loginContent.getBytes(charset));
        content = new String(Files.readAllBytes(path), charset);
        for (int i = 0; i < styles.size(); i++) {
            content = content.replaceAll(styles.get(i).get("name").split("/")[1] +": .*;" , styles.get(i).get("name").split("/")[1]+": "+styles.get(i).get("value")+";");
            Files.write(path, content.getBytes(charset));

        }
    }

    private static void copyImage(Blob image, String path) throws IOException {
        if(image != null){
            File dest = new File(path);
            image.transferTo(dest);
        }
    }
}













